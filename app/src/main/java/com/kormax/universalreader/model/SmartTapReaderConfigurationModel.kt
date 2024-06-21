package com.kormax.universalreader.model

import com.kormax.universalreader.ECKeyPairSerializer
import com.kormax.universalreader.UByteArraySerializer
import com.kormax.universalreader.google.smarttap.SmartTapFlagCheckout
import com.kormax.universalreader.google.smarttap.SmartTapFlagCvm
import com.kormax.universalreader.google.smarttap.SmartTapFlagSystem
import com.kormax.universalreader.google.smarttap.SmartTapFlagUi
import com.kormax.universalreader.google.smarttap.SmartTapMode
import com.kormax.universalreader.google.smarttap.SmartTapReaderConfiguration
import com.kormax.universalreader.toULong
import java.security.KeyPair
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@SerialName("google_smart_tap")
data class SmartTapReaderConfigurationModel(
    override val id: String,
    override val label: String,
    override val type: String,
    @Serializable(with = UByteArraySerializer::class)
    @JsonNames("session_id")
    val sessionId: UByteArray? = null,
    @Serializable(with = UByteArraySerializer::class) val nonce: UByteArray? = null,
    @Serializable(with = ECKeyPairSerializer::class)
    @JsonNames("ephemeral_key")
    val ephemeralKey: KeyPair? = null,
    @JsonNames("system", "system_flags")
    @Serializable(with = SmartTapFlagSystem.SetSerializer::class)
    val systemFlags: Set<SmartTapFlagSystem> = emptySet(),
    @JsonNames("ui", "ui_flags")
    @Serializable(with = SmartTapFlagUi.SetSerializer::class)
    val uiFlags: Set<SmartTapFlagUi> = emptySet(),
    @JsonNames("checkout", "checkout_flags")
    @Serializable(with = SmartTapFlagCheckout.SetSerializer::class)
    val checkoutFlags: Set<SmartTapFlagCheckout> = emptySet(),
    @JsonNames("cvm", "cvm_flags")
    @Serializable(with = SmartTapFlagCvm.SetSerializer::class)
    val cvmFlags: Set<SmartTapFlagCvm> = emptySet(),
    @JsonNames("mode", "vas_mode", "vasMode")
    @Serializable(with = SmartTapMode.Serializer::class)
    val mode: SmartTapMode = SmartTapMode.PASS_OVER_PAYMENT,
    val collector: SmartTapCollectorConfigurationModel
) : ProtocolModel() {
    fun load(): SmartTapReaderConfiguration {
        return SmartTapReaderConfiguration(
            collector = collector.load(),
            sessionId = sessionId?.toULong(),
            readerNonce = nonce,
            readerEphemeralKeyPair = ephemeralKey,
            systemFlags = systemFlags.toList(),
            uiFlags = uiFlags.toList(),
            checkoutFlags = checkoutFlags.toList(),
            cvmFlags = cvmFlags.toList(),
            mode = mode,
        )
    }
}
