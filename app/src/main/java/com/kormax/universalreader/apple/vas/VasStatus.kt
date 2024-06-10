package com.kormax.universalreader.apple.vas

enum class VasStatus(val sw1: UByte, val sw2: UByte) {
    SUCCESS(0x90U, 0x00U),
    DATA_NOT_FOUND(0x6aU, 0x83U),
    // Waiting for intervention
    DATA_NOT_ACTIVATED(0x62U, 0x87U),
    WRONG_PARAMETERS(0x6BU, 0x00U),
    WRONG_LC_FIELD(0x67U, 0x00U),
    USER_INTERVENTION(0x69U, 0x84U),
    INCORRECT_DATA(0x6aU, 0x80U);

    companion object {
        fun from(sw1: UByte, sw2: UByte): VasStatus? =
            VasStatus.entries.firstOrNull { it.sw1 == sw1 && it.sw2 == sw2 }
    }
}
