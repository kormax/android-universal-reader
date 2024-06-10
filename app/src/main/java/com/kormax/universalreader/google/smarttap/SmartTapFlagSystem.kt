package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.MaskEnumSetSerializer
import com.kormax.universalreader.enums.UByteMaskEnum
import kotlinx.serialization.KSerializer

enum class SmartTapFlagSystem(override val value: UByte) : UByteMaskEnum {
    STANDALONE(0x01U),
    EMI_INTEGRATED(0x02U),
    UNATTENDED(0x04U),
    ONLINE(0x08U),
    OFFLINE(0x10U),
    MMP(0x20U),
    ZLIB_SUPPORTED(0x40U);

    object SetSerializer : KSerializer<Set<SmartTapFlagSystem>> by MaskEnumSetSerializer(
        {encoder, value -> UByteMaskEnum.serializeToSet<SmartTapFlagSystem>(encoder, value)},
        {decoder -> UByteMaskEnum.deserializeToSet<SmartTapFlagSystem>(decoder)}
    )
}
