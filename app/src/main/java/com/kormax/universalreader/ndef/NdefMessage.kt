package com.kormax.universalreader.ndef

import android.content.res.Resources.NotFoundException
import com.kormax.universalreader.structable.Packable
import com.kormax.universalreader.structable.Unpackable
import com.kormax.universalreader.toUByteArray
import com.kormax.universalreader.toUInt

class NdefMessage(val records: Collection<NdefRecord>) : Packable {
    constructor(vararg records: NdefRecord?) : this(records.filterNotNull())

    constructor(array: UByteArray) : this(getRecordsFromUByteArray(array))

    constructor(string: String) : this(getRecordsFromUByteArray(string.hexToUByteArray()))

    override fun toUByteArray(): UByteArray {
        var result = ubyteArrayOf()
        for ((index, record) in records.withIndex()) {
            val payload = record.payload

            val mb = 0b10000000u * if (index == 0) 1u else 0u // Message begin
            val me = 0b01000000u * if (index == records.size - 1) 1u else 0u // Message end
            val ch = 0b00100000u * 0u // Chunk flag
            val sr = 0b00010000u * if (payload.size <= 255) 1u else 0u // Short record
            val il = 0b00001000u * if (record.id.isNotEmpty()) 1u else 0u // ID length present
            val tnf = record.tnf.toUInt() // Record type

            val header = mb or me or ch or sr or il or tnf

            val idLength =
                if (record.id.isNotEmpty()) ubyteArrayOf(record.id.size.toUByte())
                else ubyteArrayOf()
            val payloadLength =
                if (sr > 0u) ubyteArrayOf(payload.size.toUByte())
                else payload.size.toUInt().toUByteArray()

            result +=
                ubyteArrayOf(
                    header.toUByte(),
                    record.type.size.toUByte(),
                    *payloadLength,
                    *idLength,
                    *record.type,
                    *record.id,
                    *record.payload)
        }
        return result
    }

    override fun toString(): String {
        return "NdefMessage(records=${records})"
    }

    fun findByTypeOrIdElseThrow(typeOrId: String) =
        findByTypeOrIdElseThrow(typeOrId.encodeToByteArray().toUByteArray())

    fun findByTypeOrIdElseThrow(typeOrId: Packable) =
        findByTypeOrIdElseThrow(typeOrId.toUByteArray())

    fun findByTypeOrIdElseThrow(typeOrId: UByteArray): NdefRecord {
        val qualifier = typeOrId
        val result =
            records.find { it.type.contentEquals(qualifier) || it.id.contentEquals(qualifier) }
        if (result == null) {
            throw NotFoundException("NDEF record with type or id ${typeOrId} not found")
        }
        return result
    }

    fun findByTypeElseThrow(type: String) =
        findByTypeOrIdElseThrow(type.encodeToByteArray().toUByteArray())

    fun findByTypeElseThrow(type: Packable) = findByTypeOrIdElseThrow(type.toUByteArray())

    fun findByTypeElseThrow(type: UByteArray): NdefRecord {
        val result = records.find { it.type.contentEquals(type) }
        if (result == null) {
            throw NotFoundException("NDEF record with type or id ${type} not found")
        }
        return result
    }

    fun findByIdElseThrow(id: String): NdefRecord {
        val qualifier = id.encodeToByteArray().toUByteArray()
        val result = records.find { it.id.contentEquals(qualifier) }
        if (result == null) {
            throw NotFoundException("NDEF record with id ${id} not found")
        }
        return result
    }

    companion object : Unpackable<NdefMessage> {
        private fun getRecordsFromUByteArray(array: UByteArray): Collection<NdefRecord> {
            val records = mutableListOf<NdefRecord>()
            var index = 0
            var mb: Boolean
            var me: Boolean = false

            while (index < array.size - 1) {
                mb = (array[index] and 0b10000000u) > 0u // Message begin
                me = (array[index] and 0b01000000u) > 0u // Message end
                val ch = (array[index] and 0b00100000u) > 0u // Chunk flag
                val sr = (array[index] and 0b00010000u) > 0u // Short record
                val il = (array[index] and 0b00001000u) > 0u // ID length present
                val tnf = array[index] and 0b00000111u // Record type
                index += 1

                if (ch) {
                    throw Exception("Chunk flag is not yet supported")
                }
                if (records.size == 0 && !mb) {
                    throw Exception("Ndef message lacks message begin flag in the first record")
                }

                val typeLength = array[index].toUInt().toInt()
                index += 1

                val payloadLength: Int
                if (sr) {
                    payloadLength = array[index].toUInt().toInt()
                    index += 1
                } else {
                    payloadLength = array.copyOfRange(index, index + 4).toUInt().toInt()
                    index += 4
                }

                val idLength: Int
                if (il) {
                    idLength = array[index].toUInt().toInt()
                    index += 1
                } else {
                    idLength = 0
                }

                val type = array.copyOfRange(index, index + typeLength)
                index += type.size
                val id = array.copyOfRange(index, index + idLength)
                index += id.size
                val payload = array.copyOfRange(index, index + payloadLength)
                index += payload.size
                records += NdefRecord(tnf = tnf, id = id, type = type, payload = payload)
            }
            if (!me) {
                throw Exception("Ndef message lacks message end flag the last record")
            }

            return records
        }

        override fun fromUByteArray(array: UByteArray): NdefMessage {
            return NdefMessage(getRecordsFromUByteArray(array))
        }
    }
}
