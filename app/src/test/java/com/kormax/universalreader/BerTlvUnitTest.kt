package com.kormax.universalreader

import com.kormax.universalreader.tlv.ber.BerTlvMessage
import com.kormax.universalreader.tlv.ber.BerTlvTagClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class BerTlvUnitTest {
    @Test
    fun parsePpse() {
        val ppseData = (
            "6f33840e325041592e5359532e4444463031a521bf0c1e611c4f07" +
            "a000000004306050074d41455354524f8701019f0a0400010101"
        ).hexToUByteArray()
        val tlvMessage = BerTlvMessage.fromUByteArray(ppseData)
        assertEquals(tlvMessage.tags.size, 1)
        var fciTemplateTlv = tlvMessage.tags.find { it.tag.toHexString() == "6f" }
        if (fciTemplateTlv == null) {
            throw AssertionError("tlv is null")
        }
        assertEquals(fciTemplateTlv.tag.toHexString(), "6f")
        assertEquals(fciTemplateTlv.tagClass, BerTlvTagClass.APPLICATION)
        assertTrue(fciTemplateTlv.tagIsConstructed)
        var fciTemplateTlvMessage = BerTlvMessage.fromUByteArray(fciTemplateTlv.value)
        assertEquals(fciTemplateTlvMessage.tags.size, 2)
    }
}
