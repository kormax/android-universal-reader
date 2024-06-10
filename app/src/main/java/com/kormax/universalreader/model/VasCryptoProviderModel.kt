package com.kormax.universalreader.model

import com.kormax.universalreader.apple.vas.VasCryptoProvider
import kotlinx.serialization.Serializable

@Serializable
sealed class VasCryptoProviderModel {
    abstract val type: String

    abstract fun load(): VasCryptoProvider
}
