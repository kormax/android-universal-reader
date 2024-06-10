package com.kormax.universalreader.iso7816

import com.kormax.universalreader.isHexFormat

enum class Iso7816Aid(val aid: UByteArray) {
    VAS("OSE.VAS.01"),
    PPSE("2PAY.SYS.DDF01"),
    @Deprecated("Unsupported in new software versions")
    SMART_TAP_V1("a000000476d0000101"),
    SMART_TAP_V2("a000000476d0000111");

    constructor(
        aid: String
    ) : this(
        if (aid.isHexFormat()) aid.hexToUByteArray() else aid.encodeToByteArray().toUByteArray()
    )
}
