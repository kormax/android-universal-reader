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
        fun getTagTypeLengthValueFromUByteArray(array: UByteArray): Triple<UByteArray, UByteArray, UByteArray> {
            var index = 0
            var tag = mutableListOf<UByte>()

            tag.add(array[index])
            var tagNumber = array[index] and 0b00011111u
            var tagExtensionLeft = tagNumber and 0b00011111u.toUByte() == 0b00011111u.toUByte()
            index += 1
            while (tagExtensionLeft) {
                var tagExtension = array[index]
                tag.add(tagExtension)
                tagExtensionLeft = (tagExtension and 0b10000000u) > 0u
                index += 1
            }

            var length = mutableListOf<UByte>()
            var lengthBaseData = array[index]
            var lengthFormIsSimple = (lengthBaseData.inv() and 128u) > 0u

            if (lengthFormIsSimple) {
                length.add(lengthBaseData and 0b01111111u)
                index += length.size
            } else {
                var lengthLength = (lengthBaseData and 0b01111111u).toInt()
                index += 1
                if (lengthLength > 0) {
                    // Definite form
                    length.addAll(array.copyOfRange(index, index + lengthLength))
                    index += length.size
                } else {
                    // Indefinite form
                    while (
                        length.size < 2 ||
                            (length[length.size - 1].toUInt() == 0x00u &&
                                length[length.size - 2].toUInt() == 0x00u)
                    ) {
                        index += 1
                        length.add(array[index])
                    }
                }
            }
            var lengthValue = length.toUByteArray().toUInt()
            val data = array.copyOfRange(index, index + lengthValue.toInt())
            return Triple(tag.toUByteArray(), length.toUByteArray(), data)
        }

        override fun fromUByteArray(array: UByteArray): BerTlv {
            val (type, _, value) = getTagTypeLengthValueFromUByteArray(array)
            return BerTlv(type, value)
        }
    }
}
