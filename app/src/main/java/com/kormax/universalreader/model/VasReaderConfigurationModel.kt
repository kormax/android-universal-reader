package com.kormax.universalreader.model

import com.kormax.universalreader.UByteArraySerializer
import com.kormax.universalreader.apple.vas.VasMode
import com.kormax.universalreader.apple.vas.VasProtocolMode
import com.kormax.universalreader.apple.vas.VasReaderConfiguration
import com.kormax.universalreader.apple.vas.VasTerminalType
import com.kormax.universalreader.apple.vas.VasVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@SerialName("apple_vas")
data class VasReaderConfigurationModel(
    override val id: String,
    override val label: String,
    override val type: String,
    @Serializable(with = VasMode.Serializer::class)
    @JsonNames("mode", "vas_mode")
    val vasMode: VasMode,
    @Serializable(with = VasTerminalType.Serializer::class)
    @JsonNames("terminal_type")
    val terminalType: VasTerminalType,
    @Serializable(with = VasVersion.Serializer::class)
    @JsonNames("protocol_version")
    val protocolVersion: VasVersion,
    @Serializable(with = VasProtocolMode.Serializer::class)
    @JsonNames("protocol_mode")
    val protocolMode: VasProtocolMode,
    @JsonNames("vas_supported") val vasSupported: Boolean = true,
    @JsonNames("auth_required") val authRequired: Boolean = false,
    val active: List<String>,
    @Serializable(with = UByteArraySerializer::class) val nonce: UByteArray?,
    val merchants: List<VasMerchantConfigurationModel>
) : ProtocolModel() {
    fun load(): VasReaderConfiguration {
        return VasReaderConfiguration(
            vasMode = vasMode,
            terminalType = terminalType,
            protocolVersion = protocolVersion,
            protocolMode = protocolMode,
            vasSupported = vasSupported,
            authRequired = authRequired,
            nonce = nonce,
            merchants = merchants.filter { it.id in active }.map { it.load() }
        )
    }
}
