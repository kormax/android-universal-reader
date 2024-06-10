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
            for (status in
                arrayOf(
                    VasStatus.DATA_NOT_ACTIVATED,
                    VasStatus.SUCCESS,
                    VasStatus.DATA_NOT_FOUND,
                )) {
                for (result in read.filter { it.status == status }) {
                    return when (result.status) {
                        VasStatus.DATA_NOT_FOUND -> ValueAddedServicesStatus.DATA_NOT_FOUND
                        VasStatus.DATA_NOT_ACTIVATED ->
                            ValueAddedServicesStatus.WAITING_FOR_AUTHENTICATION
                        VasStatus.USER_INTERVENTION ->
                            ValueAddedServicesStatus.WAITING_FOR_SELECTION
                        VasStatus.INCORRECT_DATA -> ValueAddedServicesStatus.ERROR
                        VasStatus.WRONG_LC_FIELD -> ValueAddedServicesStatus.ERROR
                        VasStatus.WRONG_PARAMETERS -> ValueAddedServicesStatus.ERROR
                        VasStatus.SUCCESS -> ValueAddedServicesStatus.SUCCESS
                    }
                }
            }
            return ValueAddedServicesStatus.UNAVAILABLE
        }
}
