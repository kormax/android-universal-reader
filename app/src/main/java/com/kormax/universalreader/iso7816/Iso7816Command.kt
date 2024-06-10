package com.kormax.universalreader.iso7816

import com.kormax.universalreader.structable.Packable
import com.kormax.universalreader.structable.Unpackable

class Iso7816Command(
    val cla: UByte,
    val ins: UByte,
    val p1: UByte,
    val p2: UByte,
    val data: UByteArray = ubyteArrayOf(),
    // Expected response length
    val le: UByte? = null
) : Packable {

    constructor(
        cla: String,
        ins: String,
        p1: String,
        p2: String,
        data: String,
        le: String?
    ) : this(
        cla.toUByte(),
        ins.toUByte(),
        p1.toUByte(),
        p2.toUByte(),
        data.hexToUByteArray(),
        le?.toUByte()
    )

    constructor(
        cla: String,
        ins: String,
        p1: String,
        p2: String,
        data: Packable,
        le: String?
    ) : this(
        cla.toUByte(),
        ins.toUByte(),
        p1.toUByte(),
        p2.toUByte(),
        data.toUByteArray(),
        le?.toUByte()
    )

    constructor(
        cla: UByte,
        ins: UByte,
        p1: UByte,
        p2: UByte,
        data: Packable,
        le: UByte?
    ) : this(
        cla,
        ins,
        p1,
        p2,
        data.toUByteArray(),
        le,
    )

    override fun toUByteArray(): UByteArray {
        val lc =
            if (data.isEmpty()) {
                ubyteArrayOf()
            } else if (data.size > 255) {
                ubyteArrayOf(
                    0x00U,
                    (data.size and 0xFF00 shr 8).toUByte(),
                    (data.size and 0xFF).toUByte()
                )
            } else {
                ubyteArrayOf(data.size.toUByte())
            }

        // Not using ubyteArrayOf here to prevent Kotlin compiler crash (bug in Kotlin)
        var le = mutableListOf<UByte>()
        if (this.le != null) {
            le = mutableListOf(this.le)
        }

        return ubyteArrayOf(cla, ins, p1, p2, *lc, *data, *le.toUByteArray())
    }

    companion object : Unpackable<Iso7816Command> {
        override fun fromUByteArray(array: UByteArray): Iso7816Command {
            if (array.size < 4) {
                throw IllegalArgumentException(
                    "Byte array containing Iso7816Command must be at least 4 bytes long"
                )
            }

            val cla = array[0]
            val ins = array[1]
            val p1 = array[2]
            val p2 = array[3]

            if (array.size == 4) {
                // No LC, DATA, LE
                return Iso7816Command(cla, ins, p1, p2)
            } else if (array.size == 5) {
                // Only LE
                val le = array[5]
                return Iso7816Command(cla, ins, p1, p2, ubyteArrayOf(), le)
            }
            var lcDataLe = array.copyOfRange(4, array.size)
            var lc = lcDataLe[0].toInt()
            var dataLe: UByteArray
            if (lc == 0) {
                // Extended APDU
                lc = lcDataLe[1].toInt() shl 8 and lcDataLe[2].toInt()
                dataLe = lcDataLe.copyOfRange(3, lcDataLe.size)
            } else {
                dataLe = lcDataLe.copyOfRange(1, lcDataLe.size)
            }

            if (lc < dataLe.size - 1 || lc > dataLe.size) {
                throw IllegalArgumentException(
                    "LC value does not match size of rest of data ${lcDataLe.toHexString()}"
                )
            }
            val data = dataLe.copyOfRange(0, lc)
            val le = if (dataLe.size - data.size == 1) dataLe.last() else null

            return Iso7816Command(cla, ins, p1, p2, data, le)
        }

        fun selectAid(aid: Iso7816Aid): Iso7816Command {
            return selectAid(aid.aid)
        }

        fun selectAid(aid: String): Iso7816Command {
            try {
                return selectAid(aid.hexToUByteArray())
            } catch (e: IllegalArgumentException) {
                return selectAid(aid.encodeToByteArray())
            }
        }

        fun selectAid(aid: ByteArray): Iso7816Command = selectAid(aid.toUByteArray())

        fun selectAid(aid: UByteArray): Iso7816Command {
            return selectFile(
                cla = 0x00u,
                p1 = 0x04u,
                p2 = 0x00u,
                data = aid,
                le = 0x00u,
            )
        }

        fun selectFile(
            cla: UByte,
            p1: UByte,
            p2: UByte,
            data: UByteArray,
            le: UByte?
        ): Iso7816Command {
            return Iso7816Command(
                cla = cla,
                ins = 0xA4u,
                p1 = p1,
                p2 = p2,
                data = data,
                le = le,
            )
        }

        fun getData(cla: UByte, p1: UByte, p2: UByte, data: Packable, le: UByte?): Iso7816Command {
            return Iso7816Command(
                cla = cla,
                ins = 0xCAu,
                p1 = p1,
                p2 = p2,
                data = data.toUByteArray(),
                le = le,
            )
        }
    }


    override fun toString(): String {
        return "Iso7816Command(" +
                "cla=${cla.toHexString()}, ins=${ins.toHexString()}" +
                ", p1=${p1.toHexString()}, p2=${p2.toHexString()}" +
                (if (data.isNotEmpty()) ", data=${data.toHexString()}" else "") +
                (if (le != null) ", le=${le}" else "") +
                ")"
    }
}
