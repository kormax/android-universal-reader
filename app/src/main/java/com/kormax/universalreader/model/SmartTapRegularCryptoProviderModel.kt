package com.kormax.universalreader.model

import com.kormax.universalreader.ECKeyPairSerializer
import com.kormax.universalreader.google.smarttap.SmartTapRegularCryptoProvider
import java.security.KeyPair
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("regular")
data class SmartTapRegularCryptoProviderModel(
    override val type: String,
    val keys: Map<UInt, @Serializable(with = ECKeyPairSerializer::class) KeyPair?>
) : SmartTapCryptoProviderModel() {
    override fun load(): SmartTapRegularCryptoProvider {
        return SmartTapRegularCryptoProvider(
            keyPairByVersion =
                keys.entries
                    .associate { it.key to it.value }
                    .mapNotNull { it.value?.let { value -> it.key to value } }
                    .toMap()
        )
    }
}
