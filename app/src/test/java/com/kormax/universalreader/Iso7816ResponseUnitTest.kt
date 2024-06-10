package com.kormax.universalreader

import com.kormax.universalreader.iso7816.Iso7816Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class Iso7816ResponseUnitTest {
    @Test
    fun parseSelectPpseResponse() {
        val selectPppseResponseData = (
            "6f33840e325041592e5359532e4444463031a521bf0c1e611c4f07" +
            "a000000004306050074d41455354524f8701019f0a04000101019000"
        ).hexToUByteArray()
        val selectPppseResponse = Iso7816Response.fromUByteArray(selectPppseResponseData)
        assertEquals(selectPppseResponse.sw1.toHexString(), "90")
        assertEquals(selectPppseResponse.sw2.toHexString(), "00")
        assertEquals(selectPppseResponse.data.size, selectPppseResponseData.size - 2)
        assertTrue(
            selectPppseResponse.data.contentEquals(
                selectPppseResponseData.copyOfRange(
                    0,
                    selectPppseResponseData.size - 2
                )
            )
        )
    }
}
