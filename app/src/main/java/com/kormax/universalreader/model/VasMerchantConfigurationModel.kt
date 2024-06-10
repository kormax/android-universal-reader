package com.kormax.universalreader.model

import com.kormax.universalreader.apple.vas.VasMerchantConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.net.MalformedURLException
import java.net.URL

@Serializable
data class VasMerchantConfigurationModel(
    val id: String,
    val label: String,
    @JsonNames("pass_type_identifier") val passTypeIdentifier: String,
    @JsonNames("signup_url") val signupUrl: String? = null,
    @JsonNames("crypto_providers") val cryptoProviders: List<VasCryptoProviderModel> = emptyList()
) {
    fun load(): VasMerchantConfiguration {
        val url =
            try {
                URL(signupUrl)
            } catch (e: MalformedURLException) {
                null
            }
        return VasMerchantConfiguration(
            passTypeIdentifier = passTypeIdentifier,
            signupUrl = url,
            cryptoProviders = cryptoProviders.map { it.load() },
            filter = ubyteArrayOf()
        )
    }
}
