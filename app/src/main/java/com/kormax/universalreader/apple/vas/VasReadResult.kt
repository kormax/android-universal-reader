package com.kormax.universalreader.apple.vas

import java.time.LocalDateTime

class VasReadResult(
    val status: VasStatus,
    val passTypeIdentifier: String,
    val readAt: LocalDateTime,
    val cryptogram: UByteArray? = null,
    var payload: VasPayload? = null
) {
    override fun toString(): String {
        var prefix =
            "VasReadResult(status=${status}, passTypeIdentifier=${passTypeIdentifier}, readAt=${readAt}"
        if (payload != null) {
            return "${prefix}, payload=${payload})"
        }
        if (cryptogram == null) {
            return "${prefix})"
        }
        return "${prefix}, cryptogram=${cryptogram.toHexString()})"
    }
}
