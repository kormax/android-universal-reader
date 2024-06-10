package com.kormax.universalreader.apple.vas

import java.net.URL

open class VasMerchantConfiguration(
    open val passTypeIdentifier: String,
    open val cryptoProviders: Collection<VasCryptoProvider> = emptyList(),
    open val signupUrl: URL? = null,
    val filter: UByteArray = ubyteArrayOf(),
)
