package com.kormax.universalreader

import com.kormax.universalreader.ndef.NdefMessage
import org.junit.Assert.assertTrue
import org.junit.Test

class NdefMessageUnitTest {
    @Test
    fun unpackSelectSmartTap2Responose() {
        val selectSmartTap2ResponseData = "dc0321036d646e6d646e0455e5a03b5bf44d3f8802c55fc57acc95" +
                "4b0fe0d9ec5a10f1531467089226222c"
        val selectSmartTap2ResponseNdefMessage = NdefMessage(selectSmartTap2ResponseData)
        val mdnRecord = selectSmartTap2ResponseNdefMessage.findByTypeOrIdElseThrow("mdn")
        assertTrue(mdnRecord.type.contentEquals(mdnRecord.id))
    }

    @Test
    fun unpackNegotiateChannelCommand() {
        val negotiateSecureChannelCommandData = "d403b76e6772000194030a7365736b159a80fc8283" +
                "fd000154039f6370724fee8aa71dd52fc838962dbfa27778bfd423dc1a195631ed2445a5bd67637" +
                "d9101036f932f3eca62d1cc1915013421f00e185e1145a52d653c1e08b78a8418d37b0b00000001" +
                "940348736967043045022100abd74346b76404d7d8bf81abbaaf35e351e06e2a7157c516bb17807" +
                "ae966024902202d89821a410330bbb8c42ac22e8e2f88ecf479b2ca4325b40800026aaea7b4cb54" +
                "0305636c640400cc29bc"
        val negotiateSecureChannelCommandNdefMessage =
            NdefMessage(negotiateSecureChannelCommandData)

        val negotiateSecureChannelRecord =
            negotiateSecureChannelCommandNdefMessage.findByTypeOrIdElseThrow("ngr")

        val nestedNdefMessage = NdefMessage(
            negotiateSecureChannelRecord.payload.copyOfRange(
                2,
                negotiateSecureChannelRecord.payload.size
            )
        )

        val sessionRecord = nestedNdefMessage.findByTypeOrIdElseThrow("ses")
        val cryptographicParametersRecord = nestedNdefMessage.findByTypeOrIdElseThrow("cpr")

        assertTrue(sessionRecord.type.contentEquals("ses".encodeToByteArray().toUByteArray()))
        assertTrue(cryptographicParametersRecord.type.contentEquals("cpr".encodeToByteArray().toUByteArray()))
    }
}