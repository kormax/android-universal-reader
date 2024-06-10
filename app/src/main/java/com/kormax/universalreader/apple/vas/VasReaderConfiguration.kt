package com.kormax.universalreader.apple.vas

import android.content.res.Resources.NotFoundException
import android.nfc.tech.IsoDep
import com.kormax.universalreader.iso7816.Iso7816Command
import com.kormax.universalreader.iso7816.Iso7816Response
import com.kormax.universalreader.sha256
import com.kormax.universalreader.tlv.ber.BerTlv
import com.kormax.universalreader.tlv.ber.BerTlvMessage
import com.kormax.universalreader.toUInt
import com.kormax.universalreader.transceive
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.random.nextUBytes

open class VasReaderConfiguration(
    open val merchants: Collection<VasMerchantConfiguration>,
    open val vasMode: VasMode = VasMode.VAS_ONLY,
    open val terminalType: VasTerminalType = VasTerminalType.PAYMENT,
    open val protocolVersion: VasVersion = VasVersion.V1,
    open val protocolMode: VasProtocolMode = VasProtocolMode.FULL_VAS,
    open val vasSupported: Boolean = true,
    open val authRequired: Boolean = false,
    open val nonce: UByteArray? = null
) {
    suspend fun read(
        isoDep: IsoDep,
        response: Iso7816Response,
        hook: (String, Any) -> Unit = { _, _ -> }
    ): VasResult {
        var tlvMessage = BerTlvMessage(response.data).findByTagElseThrow("6f")
        val mobileCapabilitiesTag = tlvMessage.findByTagElseThrow("9f23")
        hook("log", "mobileCapabilitiesTag=${mobileCapabilitiesTag.value.toHexString()}")
        val versionNumberTag = tlvMessage.findByTagElseThrow("9f21")
        hook("log", "versionNumberTag=${versionNumberTag.value.toHexString()}")
        val mobileNonceTag = tlvMessage.findByTagElseThrow("9f24")

        val readResults = mutableListOf<VasReadResult>()

        for ((index, merchant) in merchants.withIndex()) {
            try {
                hook("log", "merchant=${merchant.passTypeIdentifier}")
                val readResult = readSingle(isoDep, merchant, last = index == merchants.size, hook)
                readResults += readResult
                if (readResult.status == VasStatus.DATA_NOT_ACTIVATED) {
                    break
                }
            } catch (e: Exception) {
                hook("exception", e)
                break
            } /*if (readResult.status == VasStatus.SUCCESS && vasMode == VasMode.VAS_ONLY) {
                  // Only logical to read one pass in VAS ONLY mode
                  // but this is commented out to give more power to the user
                  break
              }*/
        }
        return VasResult(
            readResults,
            capabilities = mobileCapabilitiesTag.value,
            version = versionNumberTag.value,
            nonce = mobileNonceTag.value
        )
    }

    private suspend fun readSingle(
        isoDep: IsoDep,
        merchant: VasMerchantConfiguration,
        last: Boolean = true,
        hook: (String, Any) -> Unit = { _, _ -> }
    ): VasReadResult {
        val terminalInfoMask =
            ((this.vasSupported.toUInt() shl 7) +
                    (this.authRequired.toUInt() shl 6) +
                    this.terminalType.value)
                .toUByte()
        val capabilitiesMask = (((!last).toUInt() shl 7) + vasMode.value).toUByte()
        hook("log", "capabilitiesMask=${capabilitiesMask.toInt().toString(radix = 2)}")

        val request =
            BerTlvMessage(
                BerTlv("9f22", protocolVersion),
                BerTlv("9f25", merchant.passTypeIdentifier.sha256()),
                BerTlv("9f26", ubyteArrayOf(0x00U, terminalInfoMask, 0x00U, capabilitiesMask)),
                BerTlv("9f28", nonce?.copyOfRange(0, 4) ?: Random.nextUBytes(4)),
                if (!merchant.filter.isEmpty()) {
                    BerTlv("9f2b", merchant.filter.copyOfRange(0, 5))
                } else null,
                if (merchant.signupUrl != null) {
                    BerTlv("9f29", merchant.signupUrl.toString())
                } else null
            )

        val readAt = LocalDateTime.now()
        val vasGetDataCommand = Iso7816Command.getData(0x80U, 0x01U, protocolMode.value, request, 0x00U)
        hook("command", vasGetDataCommand)
        val vasGetDataResponse = isoDep.transceive(vasGetDataCommand)
        hook("response", vasGetDataResponse)

        val status = VasStatus.from(vasGetDataResponse.sw1, vasGetDataResponse.sw2)

        if (status == null) {
            throw Exception("Unknown status ${vasGetDataResponse.sw.toHexString()}")
        } else if (status != VasStatus.SUCCESS) {
            return VasReadResult(
                status = status,
                readAt = readAt,
                passTypeIdentifier = merchant.passTypeIdentifier,
            )
        }

        val vasGetDataResponseTlv = BerTlvMessage(vasGetDataResponse.data).findByTagElseThrow("70")
        try {
            val unknownDataTlv = vasGetDataResponseTlv.findByTagElseThrow("9f2a")
        } catch (_: NotFoundException) {}

        val cryptogramTlv = vasGetDataResponseTlv.findByTagElseThrow("9f27")

        val cryptogram = cryptogramTlv.value

        return VasReadResult(
            status = VasStatus.SUCCESS,
            passTypeIdentifier = merchant.passTypeIdentifier,
            readAt = LocalDateTime.now(),
            cryptogram = cryptogram,
            payload =
                merchant.cryptoProviders.firstNotNullOfOrNull {
                    it.decrypt(merchant.passTypeIdentifier, cryptogram)
                }
        )
    }
}
