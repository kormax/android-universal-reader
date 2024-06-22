package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.enums.UByteMaskEnum

enum class VasMobileCapabilities(override val value: UByte) : UByteMaskEnum {
    // Only last byte is used
    // Can send data in plaintext form
    ENCRYPTION_SKIP(0b00_00_00_01U),
    // Can send data in encrypted form
    ENCRYPTION_PERFORM(0b00_00_00_10U),
    // Can skip VAS
    VAS_SKIP(0b00_00_01_00U),
    // Can perform VAS
    VAS_PERFORM(0b00_00_10_00U),
    // Can skip payment
    PAYMENT_SKIP(0b00_01_00_00U),
    // Can perform payment
    PAYMENT_PERFORM(0b00_10_00_00U);

    companion object {
        fun fromMask(value: UByte): Set<VasMobileCapabilities> {
            return UByteMaskEnum.fromMask<VasMobileCapabilities>(value)
        }
    }
}
