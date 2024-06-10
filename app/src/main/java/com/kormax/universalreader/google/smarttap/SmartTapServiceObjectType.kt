package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.UByteEnum

enum class SmartTapServiceObjectType(override val value: UByte) : UByteEnum {
    ALL_SERVICES(0x00U),
    ALL_SERVICES_EXCEPT_PPSE(0x01U),
    PPSE(0x02U),
    LOYALTY(0x03U),
    OFFER(0x04U),
    GIFT_CARD(0x05U),
    PRIVATE_LABEL_CARD(0x06U),
    EVENT_TICKET(0x07U),
    FLIGHT(0x08U),
    TRANSIT(0x09U),
    CLOUD_BASED_WALLET(0x10U),
    MOBILE_MARKETING_PLATFORM(0x11U),
    GENERIC(0x12U),
    WALLET_CUSTOMER(0x40U)
}
