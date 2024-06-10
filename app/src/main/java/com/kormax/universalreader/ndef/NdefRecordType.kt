package com.kormax.universalreader.ndef

enum class NdefRecordType(val value: UByte) {
    EMPTY(0U),
    WELL_KNOWN(1U),
    MIME(2U),
    URI(3U),
    EXTERNAL(4U),
    UNKNOWN(5U),
    UNCHANGED(6U),
    RESERVED(7U);

    companion object {
        fun from(value: UByte): NdefRecordType? =
            NdefRecordType.entries.firstOrNull { it.value == value }
    }
}
