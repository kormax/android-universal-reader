package com.kormax.universalreader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilityUnitTest {
    @Test
    fun testConvertULongToUByteArray() {
        val ulong = 283686952306183u
        assertEquals(ulong.toUByteArray().toHexString(), "0001020304050607")
    }

    @Test
    fun testConvertUIntToUByteArray() {
        val uint = 66051u
        assertEquals(uint.toUByteArray().toHexString(), "00010203")
    }

    @Test
    fun testConvertUShortToUByteArray() {
        val ushort: UShort = 1u
        assertEquals(ushort.toUByteArray().toHexString(), "0001")
    }

    @Test
    fun testConvertUByteArrayToULong() {
        val bytes = "0001020304050607".hexToUByteArray()
        assertEquals(bytes.toULong(), 283686952306183u)
    }

    @Test
    fun testConvertUByteArrayToUInt() {
        val bytes = "00010203".hexToUByteArray()
        val uint: UInt = 66051u
        assertEquals(bytes.toUInt(), uint)
    }

    @Test
    fun testConvertUByteArrayToUShort() {
        val bytes = "0001".hexToUByteArray()
        val ushort: UShort = 1U
        assertEquals(bytes.toUShort(), ushort)
    }

    @Test
    fun testIsHexFormat() {
        assertTrue("a000000476d0000101".isHexFormat())
        assertFalse("OSE.VAS.01".isHexFormat())
    }

}