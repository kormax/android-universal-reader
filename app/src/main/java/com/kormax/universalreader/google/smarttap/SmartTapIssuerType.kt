package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.UByteEnum

enum class SmartTapIssuerType(override val value: UByte) : UByteEnum {
    UNSPECIFIED(0x00U),
    MERCHANT(0x01U),
    WALLET(0x02U),
    MANUFACTURER(0x03U);

    companion object {
        infix fun from(value: UByte): SmartTapIssuerType? =
            SmartTapIssuerType.entries.firstOrNull { it.value == value }
    }
}
