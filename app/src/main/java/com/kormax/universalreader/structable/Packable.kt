package com.kormax.universalreader.structable

interface Packable {
    fun toHexString(): String = toUByteArray().toHexString()

    fun toByteArray(): ByteArray = toUByteArray().toByteArray()

    fun toUByteArray(): UByteArray
}
