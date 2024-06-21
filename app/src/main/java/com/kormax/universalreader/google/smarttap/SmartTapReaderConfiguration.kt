package com.kormax.universalreader.google.smarttap

import android.nfc.tech.IsoDep
import android.util.Log
import com.kormax.universalreader.decodeToString
import com.kormax.universalreader.decompress
import com.kormax.universalreader.getEcPublicKeyFromUBytes
import com.kormax.universalreader.getUBytesFromEcPublicKey
import com.kormax.universalreader.iso7816.Iso7816Aid
import com.kormax.universalreader.iso7816.Iso7816Command
import com.kormax.universalreader.iso7816.Iso7816Response
import com.kormax.universalreader.ndef.NdefMessage
import com.kormax.universalreader.ndef.NdefRecord
import com.kormax.universalreader.ndef.NdefRecordType
import com.kormax.universalreader.toUByte
import com.kormax.universalreader.toUByteArray
import com.kormax.universalreader.toUInt
import com.kormax.universalreader.transceive
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.random.Random
import kotlin.random.nextUBytes
import kotlin.random.nextULong

class SmartTapReaderConfiguration(
    val collector: SmartTapCollectorConfiguration,
    val sessionId: ULong? = null,
    val readerNonce: UByteArray? = null,
    val readerEphemeralKeyPair: KeyPair? = null,
    val version: SmartTapVersion = SmartTapVersion.V1,
    val systemFlags: Collection<SmartTapFlagSystem> = emptyList(),
    val uiFlags: Collection<SmartTapFlagUi> = emptyList(),
    val checkoutFlags: Collection<SmartTapFlagCheckout> = emptyList(),
    val cvmFlags: Collection<SmartTapFlagCvm> = emptyList(),
    val mode: SmartTapMode = SmartTapMode.PASS_OVER_PAYMENT,
) {
    suspend fun read(
        isoDep: IsoDep,
        response: Iso7816Response,
        hook: (String, Any) -> Unit = { _, _ -> }
    ): SmartTapResult {
        val selectSmartTap2Command = Iso7816Command.selectAid(Iso7816Aid.SMART_TAP_V2)
        hook("command", selectSmartTap2Command)
        val selectSmartTap2Response = isoDep.transceive(selectSmartTap2Command)
        hook("command", selectSmartTap2Response)

        if (selectSmartTap2Response.sw.toHexString() != "9000") {
            throw Exception("Could not select Smart Tap 2.0 applet")
        }

        hook("log", "selectSmartTap2Response=${selectSmartTap2Response}")

        var data = selectSmartTap2Response.data
        val minVersion = data.copyOfRange(0, 2).toUInt()
        val maxVersion = data.copyOfRange(2, 4).toUInt()
        hook("log", "minVersion=${minVersion} maxVersion=${maxVersion}")
        var ndef = NdefMessage(data.copyOfRange(4, data.size))

        val handsetNonceRecord = ndef.findByTypeOrIdElseThrow(SmartTapNdefType.HANDSET_NONCE)
        val deviceNonce = handsetNonceRecord.payload.copyOfRange(1, handsetNonceRecord.payload.size)
        hook("log", "deviceNonce=${deviceNonce.toHexString()}")

        var sequenceNumber: UByte = 0u
        val sessionId: ULong = this.sessionId ?: Random.nextULong()
        val readerNonce = this.readerNonce?.copyOfRange(0, 32) ?: Random.nextUBytes(32)
        val readerEphemeralKeyPair =
            this.readerEphemeralKeyPair
                ?: KeyPairGenerator.getInstance("EC")
                    .apply { initialize(ECGenParameterSpec("secp256r1"), SecureRandom()) }
                    .genKeyPair()

        val readerEphemeralPublicKey = readerEphemeralKeyPair.public as ECPublicKey
        val collectorId = collector.collectorId

        val triple =
            performNegotiateSecureChannel(
                isoDep,
                collectorId,
                readerNonce,
                deviceNonce,
                readerEphemeralPublicKey,
                sessionId,
                hook = hook)

        if (triple == null) {
            return SmartTapResult(listOf())
        }
        val (negotiateSecureChannelResponse, foundCryptoProvider, signature) = triple

        val negotiateSecureChannelResponseNdefMessage =
            NdefMessage(negotiateSecureChannelResponse.data)
        hook(
            "log",
            "negotiateSecureChannelResponseNdefMessage=${negotiateSecureChannelResponseNdefMessage}")
        val negotiateResponseRecord =
            negotiateSecureChannelResponseNdefMessage.findByTypeOrIdElseThrow(
                SmartTapNdefType.NEGOTIATE_RESPONSE)
        hook("log", "negotiateResponseRecord=${negotiateResponseRecord}")
        val handsetEphemeralPublicKeyRecord =
            negotiateResponseRecord.findByTypeOrIdElseThrow(
                SmartTapNdefType.HANDSET_EPHEMERAL_PUBLIC_KEY)
        hook("log", "handsetEphemeralPublicKeyRecord=${handsetEphemeralPublicKeyRecord}")

        val deviceEphemeralPublicKey =
            getEcPublicKeyFromUBytes(handsetEphemeralPublicKeyRecord.payload)

        val getDataResponse =
            performGetData(isoDep, collectorId, sequenceNumber, sessionId, hook = hook)
        if (getDataResponse.sw.toHexString() != "9000") {
            return SmartTapResult(listOf())
        }
        val getDataResponseNdefMessage = NdefMessage(getDataResponse.data)
        val recordBundleRecord =
            getDataResponseNdefMessage
                .findByTypeOrIdElseThrow(SmartTapNdefType.SERVICE_RESPONSE)
                .findByTypeOrIdElseThrow(SmartTapNdefType.RECORD_BUNDLE)

        var payload = recordBundleRecord.payload
        val flags = payload[0]
        hook("log", "Payload flags $flags")

        payload = payload.copyOfRange(1, payload.size)

        if (flags and 1u > 0u) {
            hook("log", "Decrypting payload ${payload.toHexString()}")
            payload =
                foundCryptoProvider.decryptPayload(
                    payload,
                    collectorId,
                    readerNonce,
                    deviceNonce,
                    signature,
                    readerEphemeralKeyPair,
                    deviceEphemeralPublicKey,
                )
            hook("log", "Decrypted payload ${payload.toHexString()}")
        }

        if (flags and 2u > 0u) {
            hook("log", "Decompressing payload ${payload.toHexString()}")
            payload = decompress(payload.toByteArray()).toUByteArray()
            hook("log", "Decompressed payload ${payload.toHexString()}")
        }

        var smartTapObjectsMessage = NdefMessage(payload)
        hook("log", "smartTapObjectsMessage=${smartTapObjectsMessage}")
        val objects = mutableListOf<SmartTapObject>()

        for (obj in smartTapObjectsMessage.records) {
            if (!obj.type.contentEquals(SmartTapNdefType.SERVICE_VALUE.toUByteArray())) {
                continue
            }
            var nested = NdefMessage(obj.payload)
            hook("log", "nested=${nested}")

            val issuerRecord = nested.findByTypeOrIdElseThrow(SmartTapNdefType.ISSUER)
            val issuerType = SmartTapIssuerType.from(issuerRecord.payload[1])
            val issuerId = issuerRecord.payload.copyOfRange(2, issuerRecord.payload.size)

            hook("log", "issuerType=${issuerType} issuerId=${issuerId.toHexString()}")
            if (issuerType == null) {
                continue
            }

            for (type in
                listOf(
                    SmartTapNdefType.CUSTOMER,
                    // Passes
                    *SmartTapNdefType.OBJECTS)) {
                try {
                    val record = nested.findByTypeOrIdElseThrow(type)
                    val snested = NdefMessage(record.payload)
                    hook("log", "record=${snested}")

                    if (type == SmartTapNdefType.CUSTOMER) {
                        val customerId = snested.findByTypeElseThrow(SmartTapNdefType.CUSTOMER_ID)
                        val language =
                            snested.findByTypeElseThrow(SmartTapNdefType.CUSTOMER_LANGUAGE).payload
                        objects +=
                            SmartTapObjectCustomer(
                                issuerId = issuerId,
                                issuerType = issuerType,
                                customerId = customerId.payload,
                                tapId = ubyteArrayOf(),
                                language = language.copyOfRange(1, language.size).decodeToString())
                    } else if (SmartTapNdefType.OBJECTS.contains(type)) {
                        val objectId = snested.findByTypeElseThrow(SmartTapNdefType.OBJECT_ID)
                        val serviceNumber =
                            snested.findByTypeElseThrow(SmartTapNdefType.SERVICE_NUMBER).payload
                        objects +=
                            SmartTapObjectPass(
                                issuerId = issuerId,
                                issuerType = issuerType,
                                type = type.toString(),
                                objectId = objectId.payload,
                                message =
                                    serviceNumber
                                        .copyOfRange(1, serviceNumber.size)
                                        .decodeToString())
                    } else {
                        hook("log", "")
                    }
                } catch (_: Exception) {}
            }
        }
        return SmartTapResult(objects)
    }

    private suspend fun performNegotiateSecureChannel(
        isoDep: IsoDep,
        collectorId: UInt,
        readerNonce: UByteArray,
        deviceNonce: UByteArray,
        readerEphemeralPublicKey: ECPublicKey,
        sessionId: ULong,
        sequenceNumber: UByte = 1u,
        hook: (String, Any) -> Unit = { _, _ -> },
    ): Triple<Iso7816Response, SmartTapCryptoProvider, UByteArray>? {
        for (cryptoProvider in collector.cryptoProviders) {
            for (keyVersion in cryptoProvider.keyVersions) {
                val signature =
                    cryptoProvider.generateReaderSignature(
                        keyVersion, collectorId, readerNonce, deviceNonce, readerEphemeralPublicKey)
                if (signature == null) {
                    continue
                }

                val message =
                    createNegotiateSecureChannelMessage(
                        readerNonce,
                        getUBytesFromEcPublicKey(readerEphemeralPublicKey),
                        signature,
                        collectorId,
                        longtermKeyVersion = keyVersion,
                        sequenceNumber = sequenceNumber,
                        sessionId = sessionId,
                        presignedAuthentication = true,
                        version = SmartTapVersion.V1,
                        status = SmartTapResponseStatus.OK,
                    )

                val negotiateSecureChannelCommand =
                    Iso7816Command(0x90U, 0x53U, 0x00U, 0x00U, message.toUByteArray(), 0x00U)
                hook("command", negotiateSecureChannelCommand)
                val negotiateSecureChannelResponse =
                    isoDep.transceive(negotiateSecureChannelCommand)
                hook("command", negotiateSecureChannelResponse)

                val status =
                    SmartTapStatus.from(
                        negotiateSecureChannelResponse.sw1, negotiateSecureChannelResponse.sw2)
                if (status == SmartTapStatus.OK) {
                    return Triple(negotiateSecureChannelResponse, cryptoProvider, signature)
                } else {
                    Log.w("SmartTap", "Wrong status ${status}")
                }
            }
        }
        return null
    }

    private fun performGetData(
        isoDep: IsoDep,
        collectorId: UInt,
        sequenceNumber: UByte = 2u,
        sessionId: ULong,
        hook: (String, Any) -> Unit = { _, _ -> }
    ): Iso7816Response {
        val payload = mutableListOf<UByte>()

        hook(
            "log",
            "Performing GET DATA with systemFlags=$systemFlags, uiFlags=$uiFlags checkoutFlags=$checkoutFlags cvmFlags=$cvmFlags")
        var command =
            Iso7816Command(
                cla = 0x90U,
                ins = 0x50U,
                p1 = 0x00U,
                p2 = 0x00U,
                data =
                    createGetDataMessage(
                        collectorId,
                        sequenceNumber,
                        sessionId,
                        mode = mode,
                        systemFlags = systemFlags,
                        uiFlags = uiFlags,
                        checkoutFlags = checkoutFlags,
                        cvmFlags = cvmFlags),
                le = 0x00U)
        while (true) {
            hook("command", command)
            val response = isoDep.transceive(command)
            hook("response", response)
            payload.addAll(response.data)
            if (!response.sw.contentEquals("9100".hexToUByteArray())) {
                hook("log", "Payload ${payload.toUByteArray().toHexString()}")
                return Iso7816Response(
                    data = payload.toUByteArray(), sw1 = response.sw1, sw2 = response.sw2)
            }
            // No need for any payload to request additional data in V 2.1
            command = Iso7816Command(cla = 0x90U, ins = 0xC0U, p1 = 0x00U, p2 = 0x00U, le = 0x00U)
        }
    }

    companion object {
        private fun createGetDataMessage(
            collectorId: UInt,
            sequenceNumber: UByte,
            sessionId: ULong,
            mode: SmartTapMode = SmartTapMode.PASS_ONLY,
            version: SmartTapVersion = SmartTapVersion.V1,
            systemFlags: Collection<SmartTapFlagSystem> = emptyList(),
            uiFlags: Collection<SmartTapFlagUi> = emptyList(),
            checkoutFlags: Collection<SmartTapFlagCheckout> = emptyList(),
            cvmFlags: Collection<SmartTapFlagCvm> = emptyList(),
        ): NdefMessage {
            return NdefMessage(
                NdefRecord(
                    tnf = NdefRecordType.EXTERNAL,
                    type = SmartTapNdefType.SERVICE_REQUEST,
                    payload =
                        listOf(
                            version,
                            NdefMessage(
                                createSessionRecord(sessionId, sequenceNumber),
                                createMerchantRecord(collectorId),
                                createServiceListRecord(),
                                createPosCapabilitiesRecord(
                                    mode = mode,
                                    systemFlags = systemFlags,
                                    uiFlags = uiFlags,
                                    checkoutFlags = checkoutFlags,
                                    cvmFlags = cvmFlags)))))
        }

        private fun createNegotiateSecureChannelMessage(
            readerNonce: UByteArray,
            readerEphemeralPublicKeyData: UByteArray,
            signature: UByteArray,
            collectorId: UInt,
            longtermKeyVersion: UInt,
            sequenceNumber: UByte,
            sessionId: ULong,
            presignedAuthentication: Boolean = true,
            version: SmartTapVersion = SmartTapVersion.V1,
            status: SmartTapResponseStatus = SmartTapResponseStatus.OK
        ): NdefMessage {
            return NdefMessage(
                NdefRecord(
                    tnf = NdefRecordType.EXTERNAL,
                    type = SmartTapNdefType.NEGOTIATE_REQUEST,
                    payload =
                        listOf(
                            version,
                            NdefMessage(
                                createSessionRecord(sessionId, sequenceNumber, status),
                                createCryptographyParametersRecord(
                                    readerNonce,
                                    readerEphemeralPublicKeyData,
                                    signature,
                                    collectorId,
                                    longtermKeyVersion,
                                    presignedAuthentication)))))
        }

        private fun createCryptographyParametersRecord(
            readerNonce: UByteArray,
            readerEphemeralPublicKeyData: UByteArray,
            signature: UByteArray,
            collectorId: UInt,
            longtermKeyVersion: UInt = 0x00000001U,
            presignedAuthentication: Boolean = true,
        ): NdefRecord {
            return NdefRecord(
                tnf = NdefRecordType.EXTERNAL,
                type = SmartTapNdefType.CRYPTO_PARAMS,
                payload =
                    ubyteArrayOf(
                        *readerNonce,
                        presignedAuthentication.toUByte(),
                        *readerEphemeralPublicKeyData,
                        *longtermKeyVersion.toUByteArray(),
                        *NdefMessage(
                                NdefRecord(
                                    tnf = NdefRecordType.EXTERNAL,
                                    type = SmartTapNdefType.SIGNATURE,
                                    payload =
                                        ubyteArrayOf(SmartTapNdefFormat.BINARY.value, *signature)),
                                NdefRecord(
                                    tnf = NdefRecordType.EXTERNAL,
                                    type = SmartTapNdefType.COLLECTOR_ID,
                                    payload =
                                        ubyteArrayOf(
                                            SmartTapNdefFormat.BINARY.value,
                                            *collectorId.toUByteArray())),
                            )
                            .toUByteArray()))
        }

        private fun createSessionRecord(
            sessionId: ULong,
            sequenceNumber: UByte,
            status: SmartTapResponseStatus = SmartTapResponseStatus.OK
        ): NdefRecord {
            return NdefRecord(
                tnf = NdefRecordType.EXTERNAL,
                type = SmartTapNdefType.SESSION,
                payload = ubyteArrayOf(*sessionId.toUByteArray(), sequenceNumber, status.value))
        }

        private fun createPosCapabilitiesRecord(
            systemFlags: Collection<SmartTapFlagSystem> = emptyList(),
            uiFlags: Collection<SmartTapFlagUi> = emptyList(),
            checkoutFlags: Collection<SmartTapFlagCheckout> = emptyList(),
            cvmFlags: Collection<SmartTapFlagCvm> = emptyList(),
            mode: SmartTapMode = SmartTapMode.PASS_ONLY,
        ): NdefRecord {
            return NdefRecord(
                tnf = NdefRecordType.EXTERNAL,
                type = SmartTapNdefType.POS_CAPABILITIES,
                payload =
                    ubyteArrayOf(
                        systemFlags.distinct().map { it.value }.sum().toUByte(),
                        uiFlags.distinct().map { it.value }.sum().toUByte(),
                        checkoutFlags.distinct().map { it.value }.sum().toUByte(),
                        cvmFlags.distinct().map { it.value }.sum().toUByte(),
                        mode.value))
        }

        private fun createServiceListRecord(
            services: Collection<SmartTapServiceObjectType> =
                listOf(SmartTapServiceObjectType.ALL_SERVICES)
        ): NdefRecord {
            return NdefRecord(
                tnf = NdefRecordType.EXTERNAL,
                type = SmartTapNdefType.SERVICE_LIST,
                payload =
                    NdefMessage(
                        services.map {
                            NdefRecord(
                                tnf = NdefRecordType.EXTERNAL,
                                type = SmartTapNdefType.SERVICE_TYPE_REQUEST,
                                payload = ubyteArrayOf(it.value))
                        }))
        }

        private fun createMerchantRecord(
            collectorId: UInt,
            locationId: UByteArray? = null,
            terminalId: UByteArray? = null,
            merchantName: String? = null,
            merchantCategoryCode: UByteArray? = null,
        ): NdefRecord {
            return NdefRecord(
                tnf = NdefRecordType.EXTERNAL,
                type = SmartTapNdefType.MERCHANT,
                payload =
                    NdefMessage(
                        NdefRecord(
                            tnf = NdefRecordType.EXTERNAL,
                            type = SmartTapNdefType.COLLECTOR_ID,
                            payload =
                                ubyteArrayOf(
                                    SmartTapNdefFormat.BINARY.value, *collectorId.toUByteArray()),
                        ),
                        if (!locationId.isNullOrEmpty())
                            NdefRecord(
                                tnf = NdefRecordType.EXTERNAL,
                                type = SmartTapNdefType.LOCATION_ID,
                                payload = locationId,
                            )
                        else null,
                        if (!terminalId.isNullOrEmpty())
                            NdefRecord(
                                tnf = NdefRecordType.EXTERNAL,
                                type = SmartTapNdefType.TERMINAL_ID,
                                payload = terminalId,
                            )
                        else null,
                        if (!merchantName.isNullOrEmpty())
                            NdefRecord(
                                tnf = NdefRecordType.WELL_KNOWN,
                                type = SmartTapNdefType.MERCHANT_NAME,
                                payload = merchantName.encodeToByteArray().toUByteArray(),
                            )
                        else null,
                        if (!merchantCategoryCode.isNullOrEmpty())
                            NdefRecord(
                                tnf = NdefRecordType.EXTERNAL,
                                type = SmartTapNdefType.MERCHANT_CATEGORY,
                                payload = merchantCategoryCode,
                            )
                        else null,
                    ))
        }
    }
}
