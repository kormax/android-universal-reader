package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.iso7816.Iso7816Response
import com.kormax.universalreader.structable.Packable

sealed class VasStatus(val sw1: UByte, val sw2: UByte) : Packable {
    // 0x90xx
    sealed class Success(sw2: UByte) : VasStatus(0x90U, sw2)

    object Ok : Success(0x00U)

    // Data exists but user/device state does not allow release yet.
    object DataNotActivated : VasStatus(0x62U, 0x87U)

    // User selection/interaction is required before data can be returned.
    object UserIntervention : VasStatus(0x69U, 0x84U)

    object WrongLcField : VasStatus(0x67U, 0x00U)

    // 0x6Axx
    sealed class DataError(sw2: UByte) : VasStatus(0x6AU, sw2)

    object IncorrectData : DataError(0x80U)

    object DataNotFound : DataError(0x83U)

    object WrongParameters : VasStatus(0x6BU, 0x00U)

    class Unknown(val status: UShort) :
        VasStatus(sw1 = (status.toUInt() shr 8).toUByte(), sw2 = status.toUByte())

    override fun toUByteArray() = ubyteArrayOf(sw1, sw2)

    override fun toString() = "${javaClass.simpleName}(${hex(sw1)}, ${hex(sw2)})"

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() =
            when (this) {
                is Success -> false
                DataNotActivated -> false
                UserIntervention -> false
                else -> true
            }

    companion object {
        private fun hex(value: UByte): String =
            "0x${value.toString(16).padStart(2, '0').uppercase()}"

        val entries: List<VasStatus> =
            listOf(
                Ok,
                DataNotFound,
                DataNotActivated,
                WrongParameters,
                WrongLcField,
                UserIntervention,
                IncorrectData,
            )

        private val bySW: Map<UInt, VasStatus> = entries.associateBy {
            (it.sw1.toUInt() shl 8) or it.sw2.toUInt()
        }

        fun from(sw1: UByte, sw2: UByte): VasStatus {
            bySW[(sw1.toUInt() shl 8) or sw2.toUInt()]?.let {
                return it
            }
            return Unknown(((sw1.toUInt() shl 8) or sw2.toUInt()).toUShort())
        }

        fun from(response: Iso7816Response): VasStatus = from(response.sw1, response.sw2)
    }
}
