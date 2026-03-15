package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.MaskEnumSetSerializer
import com.kormax.universalreader.enums.UByteMaskEnum
import kotlinx.serialization.KSerializer

enum class SmartTapFlagUi(override val value: UByte) : UByteMaskEnum {
    PRINTER(0x01U),
    PRINTER_GRAPHICS(0x02U),
    DISPLAY(0x04U),
    IMAGES(0x08U),
    AUDIO(0x10U),
    ANIMATION(0x20U),
    VIDEO(0x40U);

    object SetSerializer :
        KSerializer<Set<SmartTapFlagUi>> by MaskEnumSetSerializer(
            { encoder, value -> UByteMaskEnum.serializeToSet<SmartTapFlagUi>(encoder, value) },
            { decoder -> UByteMaskEnum.deserializeToSet<SmartTapFlagUi>(decoder) },
        )

    companion object {
        fun fromMask(value: UByte): Set<SmartTapFlagUi> {
            return UByteMaskEnum.fromMask<SmartTapFlagUi>(value)
        }
    }
}
