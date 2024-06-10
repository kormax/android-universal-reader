package com.kormax.universalreader.tlv.ber

import android.content.res.Resources.NotFoundException
import android.util.Log
import com.kormax.universalreader.structable.Packable
import com.kormax.universalreader.structable.Unpackable
import com.kormax.universalreader.toUInt

open class BerTlvMessage(val tags: List<BerTlv>) : Packable {

    constructor(array: UByteArray) : this(getTagsFromUByteArray(array))

    constructor(vararg tags: BerTlv?) : this(tags.filterNotNull())

    override fun toUByteArray(): UByteArray {
        return tags.toList().flatMap { it.toUByteArray() }.toUByteArray()
    }

    override fun toString(): String {
        return "BerTlvMessage(tags=${tags.toTypedArray().contentToString()})"
    }

    companion object : Unpackable<BerTlvMessage> {
        private fun getTagsFromUByteArray(array: UByteArray): MutableList<BerTlv> {
            var tags = mutableListOf<BerTlv>()
            var index = 0
            while (index < array.size) {
                val (tag, length, data) = BerTlv.getTagTypeLengthValueFromUByteArray(array.copyOfRange(index, array.size))
                tags.add(BerTlv(tag, data))
                index += tag.size + length.size + data.size
            }
            return tags
        }

        override fun fromUByteArray(array: UByteArray): BerTlvMessage {
            return BerTlvMessage(getTagsFromUByteArray(array))
        }
    }

    fun find(predicate: (BerTlv) -> Boolean) = tags.find(predicate)

    fun findByTagElseThrow(tag: String): BerTlv {
        return findByTagElseThrow(tag, "Tag ${tag} not found")
    }

    fun findByTagElseThrow(tag: String, message: String): BerTlv {
        val result = tags.find { it.tag.toHexString() == tag }
        if (result == null) {
            throw NotFoundException(message)
        }
        return result
    }

    fun findByTag(tag: UByteArray) = tags.find { it.tag.contentEquals(tag) }

    fun findByTag(tag: UInt) = tags.find { it.tag.toUInt() == tag }
}
