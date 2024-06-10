package com.kormax.universalreader.ndef

import com.kormax.universalreader.structable.Packable

class NdefRecord(
    val tnf: UByte,
    val type: UByteArray,
    val id: UByteArray,
    val payload: UByteArray
) {
    constructor(
        tnf: UByte,
        type: UByteArray,
        id: UByteArray,
        payload: Packable
    ) : this(tnf, type, id, payload.toUByteArray())

    constructor(
        tnf: NdefRecordType,
        type: UByteArray = ubyteArrayOf(),
        id: UByteArray = ubyteArrayOf(),
        payload: Packable
    ) : this(tnf.value, type, id, payload.toUByteArray())

    constructor(
        tnf: NdefRecordType,
        type: Packable,
        id: UByteArray = ubyteArrayOf(),
        payload: Collection<Packable>
    ) : this(
        tnf,
        type,
        id,
        payload.fold(ubyteArrayOf()) { acc, value -> ubyteArrayOf(*acc, *value.toUByteArray()) }
    )

    constructor(
        tnf: NdefRecordType,
        type: Packable,
        id: UByteArray = ubyteArrayOf(),
        payload: Packable
    ) : this(tnf.value, type.toUByteArray(), id, payload.toUByteArray())

    constructor(
        tnf: NdefRecordType,
        type: Packable,
        id: UByteArray = ubyteArrayOf(),
        payload: UByteArray
    ) : this(tnf.value, type.toUByteArray(), id, payload)

    constructor(
        tnf: NdefRecordType,
        type: UByteArray = ubyteArrayOf(),
        id: UByteArray = ubyteArrayOf(),
        payload: UByteArray = ubyteArrayOf()
    ) : this(tnf.value, type, id, payload)

    fun findByTypeOrIdElseThrow(typeOrId: Packable) =
        NdefMessage(payload).findByTypeOrIdElseThrow(typeOrId)

    fun findByTypeOrIdElseThrow(typeOrId: String) =
        NdefMessage(payload).findByTypeOrIdElseThrow(typeOrId)

    fun findByTypeElseThrow(type: String) = NdefMessage(payload).findByTypeElseThrow(type)

    fun findByIdElseThrow(id: String) = NdefMessage(payload).findByIdElseThrow(id)

    override fun toString(): String {
        val typeValue =
            try {
                String(type.toByteArray(), Charsets.UTF_8)
            } catch (e: Exception) {
                type.joinToString(separator = "") { "%02x".format(it) }
            }
        val idValue =
            try {
                String(id.toByteArray(), Charsets.UTF_8)
            } catch (e: Exception) {
                id.joinToString(separator = "") { "%02x".format(it) }
            }
        return "NdefRecord(tnf=${tnf.toHexString()}, type=${typeValue}, id=${idValue}, payload=${payload.toHexString()})"
    }
}
