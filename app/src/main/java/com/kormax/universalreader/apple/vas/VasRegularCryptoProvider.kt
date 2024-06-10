package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.sha256
import com.kormax.universalreader.toUInt
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.KDF2BytesGenerator
import org.bouncycastle.crypto.params.KDFParameters
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.ECPointUtil
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.security.KeyFactory
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec
import java.time.LocalDateTime
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class VasRegularCryptoProvider(
    val keyPairs: Collection<KeyPair>,
) : VasCryptoProvider {
    protected var keyPairsById: Map<String, KeyPair>

    init {
        keyPairsById =
            keyPairs.associate {
                val publicKey = it.public as ECPublicKey
                var publicKeyXComponentBytes = publicKey.w.affineX.toByteArray()
                // Helps to avoid a strange bug
                if (publicKeyXComponentBytes.size >= 33) {
                    publicKeyXComponentBytes = publicKeyXComponentBytes.copyOfRange(1, 33)
                }
                Pair(publicKeyXComponentBytes.sha256().copyOfRange(0, 4).toHexString(), it)
            }
    }

    override suspend fun decrypt(passTypeIdentifier: String, cryptogram: UByteArray): VasPayload? {
        val deviceKeyId = cryptogram.copyOfRange(0, 4)
        val devicePublicKeyBody = cryptogram.copyOfRange(4, 32 + 4)
        val devicePublicKeyBytes = byteArrayOf(0x02, *devicePublicKeyBody.toByteArray())
        val deviceEncryptedData = cryptogram.copyOfRange(32 + 4, cryptogram.size)

        val readerKeyPair: KeyPair =
            keyPairsById.getOrDefault(deviceKeyId.toHexString(), null) ?: return null

        try {
            val spec = ECNamedCurveTable.getParameterSpec("secp256r1")
            val kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider())
            val params = ECNamedCurveSpec("secp256r1", spec.curve, spec.g, spec.n)
            val point = ECPointUtil.decodePoint(params.curve, devicePublicKeyBytes)
            val pubKeySpec = ECPublicKeySpec(point, params)
            val devicePublicKey = kf.generatePublic(pubKeySpec) as ECPublicKey

            // Generate the shared secret
            val keyAgreement = KeyAgreement.getInstance("ECDH")
            keyAgreement.init(readerKeyPair.private)
            keyAgreement.doPhase(devicePublicKey, true)
            val sharedSecret = keyAgreement.generateSecret()

            val sharedInfo =
                byteArrayOf(0x0D) +
                    "id-aes256-GCM".encodeToByteArray() +
                    "ApplePay encrypted VAS data".encodeToByteArray() +
                    passTypeIdentifier.sha256().toByteArray()

            val sharedKey = ByteArray(32)
            val kdf = KDF2BytesGenerator(SHA256Digest())
            kdf.init(KDFParameters(sharedSecret, sharedInfo))
            kdf.generateBytes(sharedKey, 0.toShort().toInt(), 32)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(sharedKey, cipher.algorithm)
            val ivSpec = IvParameterSpec(ByteArray(16))
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val result = cipher.doFinal(deviceEncryptedData.toByteArray())

            val timedelta = result.copyOfRange(0, 4).toUByteArray().toUInt().toLong()
            val timestamp = LocalDateTime.of(2001, 1, 1, 0, 0, 0).plusSeconds(timedelta)
            val payload = result.copyOfRange(4, result.size).decodeToString()

            return VasPayload(timestamp, payload)
        } catch (e: Exception) {
            return null
        }
    }
}
