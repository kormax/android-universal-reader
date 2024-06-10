package com.kormax.universalreader.apple.vas

import java.time.LocalDateTime

class VasPayload(
    val timestamp: LocalDateTime,
    val value: String,
    val decryptionAlgorithm: VasDecryptionAlgorithm = VasDecryptionAlgorithm.V1,
) {

    override fun toString(): String {
        var prefix = "VasPayload(timestamp=${timestamp}, value=${value}"
        if (decryptionAlgorithm != VasDecryptionAlgorithm.V1) {
            return "${prefix}, decryptionAlgorithm=${decryptionAlgorithm})"
        }
        return "$prefix)"
    }
}
