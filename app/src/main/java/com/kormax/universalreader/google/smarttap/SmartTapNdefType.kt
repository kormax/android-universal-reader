package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.structable.Packable

enum class SmartTapNdefType(val value: String) : Packable {
    // Protocol
    HANDSET_NONCE("mdn"),
    SESSION("ses"),
    NEGOTIATE_REQUEST("ngr"),
    NEGOTIATE_RESPONSE("nrs"),
    CRYPTO_PARAMS("cpr"),
    SIGNATURE("sig"),
    SERVICE_REQUEST("srq"),
    ADDITIONAL_SERVICE_REQUEST("asr"),
    SERVICE_RESPONSE("srs"),
    // Merchant
    MERCHANT("mer"),
    COLLECTOR_ID_V0("mid"),
    COLLECTOR_ID("cld"),
    LOCATION_ID("lid"),
    TERMINAL_ID("tid"),
    MERCHANT_NAME("mnr"),
    MERCHANT_CATEGORY("mcr"),
    //
    SERVICE_LIST("slr"),
    SERVICE_TYPE_REQUEST("str"),
    //
    HANDSET_EPHEMERAL_PUBLIC_KEY("dpk"),
    //
    ENCRYPTED_SERVICE_VALUE("enc"),
    // srv
    SERVICE_VALUE("asv"),
    // ID
    SERVICE_ID("sid"),
    OBJECT_ID("oid"),
    RECORD_BUNDLE("reb"),
    // Customer
    CUSTOMER("cus"),
    CUSTOMER_ID("cid"),
    CUSTOMER_LANGUAGE("cpl"),
    CUSTOMER_TAP_ID("cut"),
    // SmartTap Objects
    EVENT("et"),
    FLIGHT("fl"),
    GIFT_CARD("gc"),
    LOYALTY("ly"),
    OFFER("of"),
    PLC("pl"),
    TRANSIT("tr"),
    GENERIC("gr"),
    GENERIC_PRIVATE("gp"),
    ISSUER("i"),
    SERVICE_NUMBER("n"),
    TRANSACTION_COUNTER("tcr"),
    PIN("p"),
    EXPIRATION_DATE("ex"),
    CVC("c1"),
    POS_CAPABILITIES("pcr"),
    // Service
    PUSH_SERVICE_REQUEST("spr"),
    PUSH_SERVICE_RESPONSE("psr"),
    SERVICE_STATUS("ssr"),
    SERVICE_USAGE("sug"),
    SERVICE_USAGE_TITLE("sut"),
    SERVICE_USAGE_DESCRIPTION("sud"),
    SERVICE_UPDATE("sup"),
    NEW_SERVICE("nsr"),
    NEW_SERVICE_TITLE("nst"),
    NEW_SERVICE_URI("nsu"),
    // Basket
    BASKET_PRICE("bpr"),
    BASKET_PRICE_AMOUNT("mon"),
    BASKET_PRICE_CURRENCY("ccd");

    override fun toUByteArray(): UByteArray {
        return value.encodeToByteArray().toUByteArray()
    }

    companion object {
        val OBJECTS =
            arrayOf(
                EVENT, FLIGHT, GIFT_CARD, LOYALTY, OFFER, PLC, TRANSIT, GENERIC, GENERIC_PRIVATE)
    }
}
