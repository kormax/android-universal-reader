package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.UByteEnum

enum class SmartTapNdefFormat(override val value: UByte) : UByteEnum {
    UNSPECIFIED(0x00U),
    ASCII(0x01U),
    UTF_8(0x02U),
    UTF_16(0x03U),
    BINARY(0x04U),
    BCD(0x05U)
}
