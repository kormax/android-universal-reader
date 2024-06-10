package com.kormax.universalreader.google.smarttap

class SmartTapObjectPass(
    issuerId: UByteArray,
    issuerType: SmartTapIssuerType,
    val type: String,
    val objectId: UByteArray,
    val message: String
) : SmartTapObject(issuerId, issuerType)
