package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.enums.EnumNameVariantSerializer
import com.kormax.universalreader.enums.UByteEnum
import kotlinx.serialization.KSerializer

enum class VasProtocolMode(override val value: UByte) : UByteEnum {
    URL_ONLY(0x00U),
    FULL_VAS(0x01U);

    object Serializer : KSerializer<VasProtocolMode> by EnumNameVariantSerializer({
        return@EnumNameVariantSerializer when (it) {
            "0", "00", "0x00", "url", "urlonly", "onlyurl" -> URL_ONLY
            "1", "01", "0x01", "full", "fullvas", "vasfull" -> FULL_VAS
            else -> FULL_VAS
        }
    })
}
