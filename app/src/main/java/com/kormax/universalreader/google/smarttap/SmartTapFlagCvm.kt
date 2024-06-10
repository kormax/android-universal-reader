package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.MaskEnumSetSerializer
import com.kormax.universalreader.enums.UByteMaskEnum
import kotlinx.serialization.KSerializer

enum class SmartTapFlagCvm(override val value: UByte) : UByteMaskEnum {
    ONLINE_PIN(0x01U),
    CD_PIN(0x02U),
    SIGNATURE(0x04U),
    NOCVM(0x08U),
    DEVICE_GENERATED_CODE(0x10U),
    SP_GENERATED_CODE(0x20U),
    ID_CAPTURE(0x40U),
    BIOMETRIC(0x80U);

    object SetSerializer : KSerializer<Set<SmartTapFlagCvm>> by MaskEnumSetSerializer(
        {encoder, value -> UByteMaskEnum.serializeToSet<SmartTapFlagCvm>(encoder, value)},
        {decoder -> UByteMaskEnum.deserializeToSet<SmartTapFlagCvm>(decoder)}
    )
}
