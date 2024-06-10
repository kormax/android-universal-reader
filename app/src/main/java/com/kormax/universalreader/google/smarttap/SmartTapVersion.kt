package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.structable.Packable

enum class SmartTapVersion(val major: UByte, val minor: UByte) : Packable {
    @Deprecated("Unsupported in new software versions")
    V0(0x00U, 0x00U),
    V1(0x01U, 0x00U);

    // Yes, reversed
    override fun toUByteArray() = ubyteArrayOf(minor, major)
}
