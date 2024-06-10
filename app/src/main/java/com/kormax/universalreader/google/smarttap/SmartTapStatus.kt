package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.structable.Packable

enum class SmartTapStatus(val sw1: UByte, val sw2: UByte): Packable {
    OK(0x90U, 0x00U),
    NO_PASSES(0x90U, 0x01U),
    TOO_MANY_REQUESTS(0x94U, 0x06U),
    USER_SHOULD_SELECT_CARD(0x93U, 0x02U),
    CONDITIONS_OF_USE_NOT_SATISFIED(0x69U, 0x85U);

    override fun toUByteArray() = ubyteArrayOf(sw1, sw2)

    companion object {
        fun from(sw1: UByte, sw2: UByte): SmartTapStatus? =
            SmartTapStatus.entries.firstOrNull { it.sw1 == sw1 && it.sw2 == sw2 }
    }
}
