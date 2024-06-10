package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.getUBytesFromEcPublicKey
import com.kormax.universalreader.toUByteArray
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SmartTapRegularCryptoProvider(private val keyPairByVersion: Map<UInt, KeyPair>) :
    SmartTapCryptoProvider {

    override val keyVersions: Collection<UInt>
        get() {
            return keyPairByVersion.keys
        }

    override suspend fun generateReaderSignature(
        keyVersion: UInt,
        collectorId: UInt,
        readerNonce: UByteArray,
        deviceNonce: UByteArray,
        readerEphemeralPublicKey: PublicKey
    ): UByteArray? {

        val keyPair: KeyPair = keyPairByVersion.getOrDefault(keyVersion, null) ?: return null
        val signingKey: PrivateKey = keyPair.private
        val signature = Signature.getInstance("SHA256withECDSA")

        // Generate the signature
        signature.initSign(signingKey)
        signature.update(readerNonce.toByteArray())
        signature.update(deviceNonce.toByteArray())
        signature.update(collectorId.toUByteArray().toByteArray())
        signature.update(
            getUBytesFromEcPublicKey(readerEphemeralPublicKey as ECPublicKey).toByteArray()
        )

        val signedData = signature.sign()
        return signedData.toUByteArray()
    }

    private fun generateEphemeralKeys(
        collectorId: UInt,
        readerNonce: UByteArray,
        deviceNonce: UByteArray,
        signature: UByteArray,
        sharedSecret: UByteArray,
        readerEphemeralPublicKeyData: UByteArray,
        deviceEphemeralPublicKeyData: UByteArray,
    ): Pair<SecretKey, SecretKey> {

        val info =
            readerNonce +
                deviceNonce +
                collectorId.toUByteArray() +
                readerEphemeralPublicKeyData +
                signature

        val digest: Digest = SHA256Digest()
        val hkdf2 = HKDFBytesGenerator(digest)
        hkdf2.init(
            HKDFParameters(
                sharedSecret.toByteArray(),
                deviceEphemeralPublicKeyData.toByteArray(),
                info.toByteArray()
            )
        )
        val sharedKey = ByteArray(48)
        hkdf2.generateBytes(sharedKey, 0, sharedKey.size)

        val decryptionKey: SecretKey = SecretKeySpec(sharedKey.copyOfRange(0, 16), "AES")
        val hashKey: SecretKey =
            SecretKeySpec(sharedKey.copyOfRange(16, sharedKey.size), "HmacSHA256")

        return Pair(decryptionKey, hashKey)
    }

    override suspend fun decryptPayload(
        payload: UByteArray,
        collectorId: UInt,
        readerNonce: UByteArray,
        deviceNonce: UByteArray,
        signature: UByteArray,
        readerEphemeralKeyPair: KeyPair,
        deviceEphemeralPublicKey: PublicKey,
    ): UByteArray {
        val iv = payload.copyOfRange(0, 12)
        val ciphertext = payload.copyOfRange(12, 12 + payload.size - 44)
        val hmac = payload.copyOfRange(payload.size - 32, payload.size)

        // Generate the shared secret
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(readerEphemeralKeyPair.private)
        keyAgreement.doPhase(deviceEphemeralPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret().toUByteArray()

        val (decryptionKey, hashKey) =
            this.generateEphemeralKeys(
                collectorId,
                readerNonce,
                deviceNonce,
                signature,
                sharedSecret,
                getUBytesFromEcPublicKey(readerEphemeralKeyPair.public as ECPublicKey),
                getUBytesFromEcPublicKey(deviceEphemeralPublicKey as ECPublicKey),
            )

        val hmacSha256 = Mac.getInstance("HmacSHA256")
        hmacSha256.init(hashKey)
        val derivedHmac = hmacSha256.doFinal((iv + ciphertext).toByteArray())

        if (!derivedHmac.contentEquals(hmac.toByteArray())) {
            throw Exception("${derivedHmac.toHexString()} != ${hmac.toHexString()}")
        }

        // Decrypt the payload
        val cipher = Cipher.getInstance("AES/CTR/NOPADDING")
        cipher.init(
            Cipher.DECRYPT_MODE,
            decryptionKey,
            IvParameterSpec(iv.toByteArray() + ByteArray(4))
        )
        // AES-CTR starts with 4-byte 0 counter
        return cipher.doFinal(ciphertext.toByteArray()).toUByteArray()
    }
}
