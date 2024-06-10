package com.kormax.universalreader.tlv.ber

enum class BerTlvTagClass(val mask: UInt) {
    UNIVERSAL(0b00u),
    APPLICATION(0b01u),
    CONTEXT_SPECIFIC(0b10u),
    PRIVATE(0b11u);

    companion object {
        infix fun from(value: UInt): BerTlvTagClass? = entries.firstOrNull { it.mask == value }
    }
}
