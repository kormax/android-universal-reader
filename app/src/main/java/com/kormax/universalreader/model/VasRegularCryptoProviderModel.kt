package com.kormax.universalreader.model

import com.kormax.universalreader.ECKeyPairSerializer
import com.kormax.universalreader.apple.vas.VasRegularCryptoProvider
import java.security.KeyPair
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("regular")
data class VasRegularCryptoProviderModel(
    override val type: String,
    val keys: List<@Serializable(with = ECKeyPairSerializer::class) KeyPair?>
) : VasCryptoProviderModel() {
    override fun load(): VasRegularCryptoProvider {
        return VasRegularCryptoProvider(keyPairs = keys.filterNotNull())
    }
}
