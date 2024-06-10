package com.kormax.universalreader.model

import com.kormax.universalreader.google.smarttap.SmartTapCryptoProvider
import kotlinx.serialization.Serializable

@Serializable
sealed class SmartTapCryptoProviderModel {
    abstract val type: String

    abstract fun load(): SmartTapCryptoProvider
}

