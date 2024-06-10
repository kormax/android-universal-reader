package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.EnumNameVariantSerializer
import com.kormax.universalreader.enums.UByteEnum
import kotlinx.serialization.KSerializer

enum class SmartTapMode(override val value: UByte) : UByteEnum {
    PASS_ONLY(0x01U),
    PAYMENT_ONLY(0x02U),
    PASS_AND_PAYMENT(0x04U),
    PASS_OVER_PAYMENT(0x08U);

    object Serializer : KSerializer<SmartTapMode> by EnumNameVariantSerializer({
        return@EnumNameVariantSerializer when (it) {
            "1", "01", "0x01", "passonly", "vas", "pass", "vasonly" -> PASS_ONLY
            "2", "02", "0x02", "paymentonly", "pay", "payonly", "payment" -> PAYMENT_ONLY
            "4", "04", "0x04", "passandpayment", "vasandpay", "payandvas" -> PASS_AND_PAYMENT
            "8", "08", "0x08", "passoverpayment", "vasorpay", "payorvas"  -> PASS_OVER_PAYMENT
            else -> PASS_OVER_PAYMENT
        }
    })
}
