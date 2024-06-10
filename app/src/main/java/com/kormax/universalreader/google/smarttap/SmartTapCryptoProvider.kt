package com.kormax.universalreader.google.smarttap

import java.security.KeyPair
import java.security.PublicKey

interface SmartTapCryptoProvider {
    val keyVersions: Collection<UInt>

    suspend fun generateReaderSignature(
        keyVersion: UInt,
        collectorId: UInt,
        readerNonce: UByteArray,
        deviceNonce: UByteArray,
        readerEphemeralPublicKey: PublicKey
    ): UByteArray?

    suspend fun decryptPayload(
        payload: UByteArray,
        collectorId: UInt,
        readerNonce: UByteArray,
        deviceNonce: UByteArray,
        signature: UByteArray,
        readerEphemeralKeyPair: KeyPair,
        deviceEphemeralPublicKey: PublicKey,
    ): UByteArray
}
