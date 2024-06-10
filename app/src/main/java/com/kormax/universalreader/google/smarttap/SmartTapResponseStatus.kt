package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.enums.UByteEnum

enum class SmartTapResponseStatus(override val value: UByte) : UByteEnum {
    UNKNOWN(0x00U),
    OK(0x01U),
    NDEF_FORMAT_ERROR(0x02U),
    UNSUPPORTED_VERSION(0x03U),
    INVALID_SEQUENCE_NUMBER(0x04U),
    UNKNOWN_MERCHANT(0x05U),
    MERCHANT_DATA_MISSING(0x06U),
    SERVICE_DATA_MISSING(0x07U),
    RESEND_REQUEST(0x08U),
    DATA_NOT_AVAILABLE_YET(0x09U)
}
