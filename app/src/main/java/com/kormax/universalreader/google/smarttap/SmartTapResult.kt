package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.UniversalReaderResult
import com.kormax.universalreader.UniversalReaderStatus

class SmartTapResult(
    val objects: Collection<SmartTapObject>,
    val smartTapStatus: SmartTapStatus? = null,
) : UniversalReaderResult() {
    override fun toString(): String {
        return "SmartTapResult(" +
            "objects=${objects.toTypedArray().contentToString()}, " +
            "smartTapStatus=${smartTapStatus})"
    }

    override val status: UniversalReaderStatus
        get() {
            if (objects.find { it is SmartTapObjectPass } != null) {
                return UniversalReaderStatus.SUCCESS
            }
            return when (smartTapStatus) {
                SmartTapStatus.DeviceLocked -> UniversalReaderStatus.WAITING_FOR_AUTHENTICATION
                SmartTapStatus.DisambiguationScreenShown ->
                    UniversalReaderStatus.WAITING_FOR_SELECTION
                SmartTapStatus.OkNoPayload -> UniversalReaderStatus.DATA_NOT_FOUND
                null -> UniversalReaderStatus.UNAVAILABLE
                else -> {
                    if (smartTapStatus.isSuccess) {
                        UniversalReaderStatus.UNAVAILABLE
                    } else {
                        UniversalReaderStatus.ERROR
                    }
                }
            }
        }
}
