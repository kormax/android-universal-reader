package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.enums.EnumNameVariantSerializer
import com.kormax.universalreader.enums.UByteEnum
import kotlinx.serialization.KSerializer

enum class VasTerminalType(override val value: UByte): UByteEnum {
    PAYMENT(0x00U),
    TRANSIT(0x01U),
    ACCESS(0x02U),
    WIRELESS_HANDOFF(0x03U),
    APP_HANDOFF(0x04U),
    OTHER(0x0FU);

    object Serializer : KSerializer<VasTerminalType> by EnumNameVariantSerializer({
        return@EnumNameVariantSerializer when(it) {
            "0", "00", "0x00", "payment" -> PAYMENT
            "1", "01", "0x01", "transit" -> TRANSIT
            "2", "02", "0x02", "access" -> ACCESS
            "3", "03", "0x03", "wirelesshandoff" -> WIRELESS_HANDOFF
            "4", "04", "0x04", "apphandoff" -> APP_HANDOFF
            "15", "0F", "0x0F", "other" -> OTHER
            else -> PAYMENT
        }
    })
}
