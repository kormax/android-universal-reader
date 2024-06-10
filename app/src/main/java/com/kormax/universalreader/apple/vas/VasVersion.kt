package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.enums.EnumNameVariantSerializer
import com.kormax.universalreader.structable.Packable
import kotlinx.serialization.KSerializer

enum class VasVersion(val major: UByte, val minor: UByte) : Packable {
    V1(0x01U, 0x00U);

    override fun toUByteArray(): UByteArray {
        return ubyteArrayOf(major, minor)
    }

    object Serializer : KSerializer<VasVersion> by EnumNameVariantSerializer({
        return@EnumNameVariantSerializer when (it) {
            "1.0", "1", "v1", -> V1
            else -> V1
        }
    })
}
