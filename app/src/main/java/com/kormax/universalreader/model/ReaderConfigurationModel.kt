package com.kormax.universalreader.model

import com.kormax.universalreader.ValueAddedServicesReaderConfiguration
import com.kormax.universalreader.apple.vas.VasReaderConfiguration
import com.kormax.universalreader.google.smarttap.SmartTapReaderConfiguration
import kotlinx.serialization.Serializable

@Serializable
data class ReaderConfigurationModel(val active: List<String>, val protocols: List<ProtocolModel>) {
    fun load(): ValueAddedServicesReaderConfiguration {
        var vas: VasReaderConfiguration? = null
        var smartTap: SmartTapReaderConfiguration? = null

        for (id in active) {
            val found = protocols.find { it.id == id }
            if (found is VasReaderConfigurationModel) {
                vas = found.load()
            }
            if (found is SmartTapReaderConfigurationModel) {
                smartTap = found.load()
            }
        }
        return ValueAddedServicesReaderConfiguration(vas = vas, smartTap = smartTap)
    }
}
