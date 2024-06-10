package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.ValueAddedServicesResult
import com.kormax.universalreader.ValueAddedServicesStatus

class SmartTapResult(val objects: Collection<SmartTapObject>) : ValueAddedServicesResult() {
    override fun toString(): String {
        return "SmartTapResult(objects=${objects.toTypedArray().contentToString()})"
    }

    override val status: ValueAddedServicesStatus
        get() {
            // TODO HANDLE MORE CASES
            if (objects.find {it is SmartTapObjectPass} != null) {
                return ValueAddedServicesStatus.SUCCESS
            }
            return ValueAddedServicesStatus.UNAVAILABLE
        }
}
