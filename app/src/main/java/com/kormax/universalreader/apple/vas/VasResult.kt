package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.ValueAddedServicesResult
import com.kormax.universalreader.ValueAddedServicesStatus

open class VasResult(
    val read: Collection<VasReadResult> = emptyList(),
    val version: UByteArray,
    val capabilities: UByteArray,
    val nonce: UByteArray,
) : ValueAddedServicesResult() {

    override fun toString(): String {
        return "VasResult(read=${read.toTypedArray().contentToString()})"
    }

    override val status: ValueAddedServicesStatus
        get() {
            if (read.any { it.status == VasStatus.DataNotActivated }) {
                return ValueAddedServicesStatus.WAITING_FOR_AUTHENTICATION
            }
            if (read.any { it.status == VasStatus.UserIntervention }) {
                return ValueAddedServicesStatus.WAITING_FOR_SELECTION
            }
            if (read.any { it.status.isSuccess }) {
                return ValueAddedServicesStatus.SUCCESS
            }
            if (read.any { it.status == VasStatus.DataNotFound }) {
                return ValueAddedServicesStatus.DATA_NOT_FOUND
            }
            if (read.any { it.status.isError }) {
                return ValueAddedServicesStatus.ERROR
            }
            return ValueAddedServicesStatus.UNAVAILABLE
        }
}
