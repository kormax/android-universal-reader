package com.kormax.universalreader.apple.vas

import com.kormax.universalreader.UniversalReaderResult
import com.kormax.universalreader.UniversalReaderStatus

open class VasResult(
    val read: Collection<VasReadResult> = emptyList(),
    val version: UByteArray,
    val capabilities: UByteArray,
    val nonce: UByteArray,
) : UniversalReaderResult() {

    override fun toString(): String {
        return "VasResult(read=${read.toTypedArray().contentToString()})"
    }

    override val status: UniversalReaderStatus
        get() {
            if (read.any { it.status == VasStatus.DataNotActivated }) {
                return UniversalReaderStatus.WAITING_FOR_AUTHENTICATION
            }
            if (read.any { it.status == VasStatus.UserIntervention }) {
                return UniversalReaderStatus.WAITING_FOR_SELECTION
            }
            if (read.any { it.status.isSuccess }) {
                return UniversalReaderStatus.SUCCESS
            }
            if (read.any { it.status == VasStatus.DataNotFound }) {
                return UniversalReaderStatus.DATA_NOT_FOUND
            }
            if (read.any { it.status.isError }) {
                return UniversalReaderStatus.ERROR
            }
            return UniversalReaderStatus.UNAVAILABLE
        }
}
