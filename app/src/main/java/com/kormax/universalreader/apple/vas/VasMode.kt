package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.enums.EnumNameVariantSerializer
import com.kormax.universalreader.enums.UByteEnum
import kotlinx.serialization.KSerializer

enum class VasMode(override val value: UByte) : UByteEnum {
    VAS_OR_PAY(0x00U),
    VAS_AND_PAY(0x01U),
    VAS_ONLY(0x02U),
    PAY_ONLY(0x03U);

    object Serializer : KSerializer<VasMode> by EnumNameVariantSerializer({
        return@EnumNameVariantSerializer when (it) {
            "0", "00", "0x00", "vasorpay", "payorvas" -> VAS_OR_PAY
            "1", "01", "0x01", "vasandpay", "payandvas" -> VAS_AND_PAY
            "2", "02", "0x02", "vasonly", "vas" -> VAS_ONLY
            "3", "03", "0x03", "payonly", "pay", "payment" -> PAY_ONLY
            else -> VAS_OR_PAY
        }
    })
}
