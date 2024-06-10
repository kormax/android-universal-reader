package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.MaskEnumSetSerializer
import com.kormax.universalreader.enums.UByteMaskEnum
import kotlinx.serialization.KSerializer

enum class SmartTapFlagCheckout(override val value: UByte) : UByteMaskEnum {
    PAYMENT(0x01U),
    DIGITAL_RECEIPT(0x02U),
    SERVICE_ISSUANCE(0x04U),
    OTA_POS_DATA(0x08U);

    object SetSerializer : KSerializer<Set<SmartTapFlagCheckout>> by MaskEnumSetSerializer(
        {encoder, value -> UByteMaskEnum.serializeToSet<SmartTapFlagCheckout>(encoder, value)},
        {decoder -> UByteMaskEnum.deserializeToSet<SmartTapFlagCheckout>(decoder)}
    )
}
