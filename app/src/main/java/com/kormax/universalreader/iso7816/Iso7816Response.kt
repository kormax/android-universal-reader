package com.kormax.universalreader.iso7816

import com.kormax.universalreader.structable.Packable
import com.kormax.universalreader.structable.Unpackable

class Iso7816Response(val data: UByteArray, val sw1: UByte, val sw2: UByte) : Packable {
    override fun toUByteArray(): UByteArray {
        val array = UByteArray(data.size + 2)
        data.copyInto(array, 0, 0, data.size)
        array[array.size - 2] = sw1
        array[array.size - 1] = sw2
        return array
    }

    val sw: UByteArray
        get() = ubyteArrayOf(sw1, sw2)

    override fun toString(): String {
        return "Iso7816Response(" +
            (if (data.isNotEmpty()) "data=${data.toHexString()}, " else "") +
            "sw=${sw.toHexString()})"
    }

    companion object : Unpackable<Iso7816Response> {
        override fun fromUByteArray(array: UByteArray): Iso7816Response {
            if (array.size < 2) {
                throw IllegalArgumentException("Byte array must be at least 2 bytes long")
            }

            val sw1 = array[array.size - 2]
            val sw2 = array[array.size - 1]
            val data = array.copyOfRange(0, array.size - 2)
            return Iso7816Response(data, sw1, sw2)
        }
    }
}
