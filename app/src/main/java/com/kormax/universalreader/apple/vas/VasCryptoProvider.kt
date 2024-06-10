package com.kormax.universalreader.apple.vas

interface VasCryptoProvider {
    suspend fun decrypt(passTypeIdentifier: String, cryptogram: UByteArray): VasPayload?
}
