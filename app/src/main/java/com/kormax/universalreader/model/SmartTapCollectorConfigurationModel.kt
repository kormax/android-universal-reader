package com.kormax.universalreader.model

import com.kormax.universalreader.google.smarttap.SmartTapCollectorConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SmartTapCollectorConfigurationModel(
    @JsonNames("collector_id") val collectorId: UInt,
    @JsonNames("crypto_providers") val cryptoProviders: List<SmartTapCryptoProviderModel>
) {
    fun load(): SmartTapCollectorConfiguration {
        return SmartTapCollectorConfiguration(
            collectorId = collectorId,
            cryptoProviders = cryptoProviders.map { it.load() }
        )
    }
}
