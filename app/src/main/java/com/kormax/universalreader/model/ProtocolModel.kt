package com.kormax.universalreader.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ProtocolModel {
    abstract val id: String
    abstract val label: String
    abstract val type: String
}
