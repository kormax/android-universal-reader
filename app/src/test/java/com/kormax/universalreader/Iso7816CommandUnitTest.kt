package com.kormax.universalreader

import com.kormax.universalreader.iso7816.Iso7816Command
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test


class Iso7816CommandUnitTest {
    @Test
    fun parseSelectPpseCommand() {
        val selectPpseCommandData = "00a404000e325041592e5359532e444446303100".hexToByteArray()
        val selectPppseCommand = Iso7816Command.fromByteArray(selectPpseCommandData)
        assertEquals(selectPppseCommand.cla.toHexString(), "00")
        assertEquals(selectPppseCommand.ins.toHexString(), "a4")
        assertEquals(selectPppseCommand.p1.toHexString(), "04")
        assertEquals(selectPppseCommand.p2.toHexString(), "00")
        assertEquals(selectPppseCommand.p2.toHexString(), "00")
        assertEquals(String(selectPppseCommand.data.toByteArray(), Charsets.UTF_8), "2PAY.SYS.DDF01")
        assertNotNull(selectPppseCommand.le)
        assertEquals(selectPppseCommand.le?.toHexString(), "00")
    }
}
