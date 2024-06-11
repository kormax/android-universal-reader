package com.kormax.universalreader.tlv.ber

import com.kormax.universalreader.structable.Packable
import com.kormax.universalreader.structable.Unpackable
import com.kormax.universalreader.toUInt

open class BerTlv(
    val tag: UByteArray,
    val value: UByteArray,
) : Packable {
    constructor(
        array: UByteArray,
    ) : this(getTagTypeLengthValueFromUByteArray(array))

    constructor(value: Triple<UByteArray, UByteArray, UByteArray>) : this(value.first, value.third)

    constructor(tag: String, value: Packable) : this(tag.hexToUByteArray(), value.toUByteArray())

    constructor(tag: String, value: UByteArray) : this(tag.hexToUByteArray(), value)

    constructor(
        tag: String,
        value: String
    ) : this(tag.hexToUByteArray(), value.encodeToByteArray().toUByteArray())

    val tagIsConstructed: Boolean
        get() = (tag[0].toUInt() and 0b00100000u) != 0u

    val tagClass: BerTlvTagClass?
        get() = BerTlvTagClass.from((tag[0].toUInt() and 0b11000000u) shr 6)

    fun toBerTlvMessage() = BerTlvMessage.fromUByteArray(value)

    fun findByTagElseThrow(tag: String) = toBerTlvMessage().findByTagElseThrow(tag)

    fun findByTagElseThrow(tag: String, message: String) =
        toBerTlvMessage().findByTagElseThrow(tag, message)

    override fun toUByteArray(): UByteArray {
        var length =
            if (value.size <= 127) {
                ubyteArrayOf(value.size.toUByte())
            } else {
                var size = value.size
                var lengthBytes = mutableListOf<UByte>()
                while (size >= 0xff) {
                    lengthBytes.add((size and 0xff).toUByte())
                    size = size shr 8
                }
                if (lengthBytes.size > 127) {
                    throw Exception("Value size is too big ${lengthBytes.size} > 127")
                }
                ubyteArrayOf(lengthBytes.size.toUByte(), *lengthBytes.toUByteArray())
            }
        return ubyteArrayOf(*tag, *length, *value)
    }

    override fun toString(): String {
        return "BerTlv(tag=${tag.toHexString()}, value=${value.toHexString()})"
    }

    companion object : Unpackable<BerTlv> {
        fun getTagTypeLengthValueFromUByteArray(
            array: UByteArray
        ): Triple<UByteArray, UByteArray, UByteArray> {
            var index = 0
            val tag = mutableListOf<UByte>()

            // Process the tag
            tag.add(array[index])
            var tagExtensionLeft = (array[index] and 0b00011111u) == 0b00011111u.toUByte()
            index++
            while (tagExtensionLeft) {
                val tagExtension = array[index]
                tag.add(tagExtension)
                tagExtensionLeft = (tagExtension and 0b10000000u) > 0u
                index++
            }

            // Process the length
            val length = mutableListOf(array[index])
            index++
            if ((length.first() and 0b10000000u) != 0u.toUByte()) {
                val lengthLength = (length.first() and 0b01111111u).toInt()
                if (lengthLength > 0) {
                    length.addAll(array.copyOfRange(index, index + lengthLength))
                } else {
                    throw NotImplementedError("Indefinite long form is not supported")
                }
            }
            index += length.size - 1

            // Process the value
            val lengthValue =
                if ((length.first() and 0b10000000u) == 0u.toUByte()) {
                    length.first().toUInt()
                } else {
                    length.drop(1).toUByteArray().toUInt()
                }

            val data = array.copyOfRange(index, index + lengthValue.toInt())
            return Triple(tag.toUByteArray(), length.toUByteArray(), data)
        }

        override fun fromUByteArray(array: UByteArray): BerTlv {
            val (type, _, value) = getTagTypeLengthValueFromUByteArray(array)
            return BerTlv(type, value)
        }
    }
}
