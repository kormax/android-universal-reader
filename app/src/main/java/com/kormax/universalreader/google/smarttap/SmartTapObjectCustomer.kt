package com.kormax.universalreader.google.smarttap

class SmartTapObjectCustomer(
    issuerId: UByteArray,
    issuerType: SmartTapIssuerType,
    val customerId: UByteArray,
    val tapId: UByteArray,
    val language: String
) : SmartTapObject(issuerId, issuerType)
