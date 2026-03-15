package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.MaskEnumSetSerializer
import com.kormax.universalreader.enums.UByteMaskEnum
import kotlinx.serialization.KSerializer

// Bit flags from OSE Smart Tap capability byte (DF62).
enum class SmartTapFlagCapabilities(override val value: UByte) : UByteMaskEnum {
    ALLOW_SKIPPING_SECOND_SELECT(0x01U),
    VAS_SUPPORT(0x02U);

    object SetSerializer :
        KSerializer<Set<SmartTapFlagCapabilities>> by MaskEnumSetSerializer(
            { encoder, value ->
                UByteMaskEnum.serializeToSet<SmartTapFlagCapabilities>(encoder, value)
            },
            { decoder -> UByteMaskEnum.deserializeToSet<SmartTapFlagCapabilities>(decoder) },
        )

    companion object {
        fun fromMask(value: UByte): Set<SmartTapFlagCapabilities> {
            return UByteMaskEnum.fromMask<SmartTapFlagCapabilities>(value)
        }
    }
}
