package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.MaskEnumSetSerializer
import com.kormax.universalreader.enums.UByteMaskEnum
import kotlinx.serialization.KSerializer

// Bit flags from OSE transaction mode byte (C1).
enum class SmartTapFlagTransactionMode(override val value: UByte) : UByteMaskEnum {
    PASS_REQUESTED(0x04U),
    PASS_ENABLED(0x08U),
    PAYMENT_REQUESTED(0x40U),
    PAYMENT_ENABLED(0x80U);

    object SetSerializer :
        KSerializer<Set<SmartTapFlagTransactionMode>> by MaskEnumSetSerializer(
            { encoder, value ->
                UByteMaskEnum.serializeToSet<SmartTapFlagTransactionMode>(encoder, value)
            },
            { decoder -> UByteMaskEnum.deserializeToSet<SmartTapFlagTransactionMode>(decoder) },
        )

    companion object {
        fun fromMask(value: UByte): Set<SmartTapFlagTransactionMode> {
            return UByteMaskEnum.fromMask<SmartTapFlagTransactionMode>(value)
        }
    }
}
