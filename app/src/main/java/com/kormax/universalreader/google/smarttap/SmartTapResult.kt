package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.ValueAddedServicesResult
import com.kormax.universalreader.ValueAddedServicesStatus

class SmartTapResult(
    val objects: Collection<SmartTapObject>,
    val smartTapStatus: SmartTapStatus? = null,
) : ValueAddedServicesResult() {
    override fun toString(): String {
        return "SmartTapResult(" +
            "objects=${objects.toTypedArray().contentToString()}, " +
            "smartTapStatus=${smartTapStatus})"
    }

    override val status: ValueAddedServicesStatus
        get() {
            if (objects.find { it is SmartTapObjectPass } != null) {
                return ValueAddedServicesStatus.SUCCESS
            }
            return when (smartTapStatus) {
                SmartTapStatus.DeviceLocked -> ValueAddedServicesStatus.WAITING_FOR_AUTHENTICATION
                SmartTapStatus.DisambiguationScreenShown ->
                    ValueAddedServicesStatus.WAITING_FOR_SELECTION
                SmartTapStatus.OkNoPayload -> ValueAddedServicesStatus.DATA_NOT_FOUND
                null -> ValueAddedServicesStatus.UNAVAILABLE
                else -> {
                    if (smartTapStatus.isSuccess) {
                        ValueAddedServicesStatus.UNAVAILABLE
                    } else {
                        ValueAddedServicesStatus.ERROR
                    }
                }
            }
        }
}
