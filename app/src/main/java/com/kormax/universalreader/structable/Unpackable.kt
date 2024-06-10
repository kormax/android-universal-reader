package com.kormax.universalreader.structable

interface Unpackable<T> {
    fun fromHexString(string: String): T = fromUByteArray(string.hexToUByteArray())

    fun fromByteArray(array: ByteArray): T = fromUByteArray(array.toUByteArray())

    fun fromUByteArray(array: UByteArray): T
}
