package com.kormax.universalreader.model

import android.os.Bundle
import com.kormax.universalreader.UniversalReaderConfiguration
import com.kormax.universalreader.apple.vas.VasReaderConfiguration
import com.kormax.universalreader.google.smarttap.SmartTapReaderConfiguration
import kotlinx.serialization.Serializable

@Serializable
data class ReaderConfigurationModel(
    val active: List<String>,
    val protocols: List<ProtocolModel>,
    val extras: Map<String, ReaderModeExtraModel> = emptyMap(),
) {
    fun load(): UniversalReaderConfiguration {
        var vas: VasReaderConfiguration? = null
        var smartTap: SmartTapReaderConfiguration? = null

        for (id in active) {
            val found = protocols.find { it.id == id }
            if (found is VasReaderConfigurationModel) {
                vas = found.load()
            }
            if (found is SmartTapReaderConfigurationModel) {
                smartTap = found.load()
            }
        }
        return UniversalReaderConfiguration(vas = vas, smartTap = smartTap)
    }

    fun loadReaderModeExtrasBundle(): Bundle? {
        if (extras.isEmpty()) {
            return null
        }

        return Bundle().apply {
            for ((key, extra) in extras) {
                extra.putIn(this, key)
            }
        }
    }
}

@Serializable
data class ReaderModeExtraModel(val type: String, val value: String) {
    fun putIn(bundle: Bundle, key: String) {
        when (type.lowercase()) {
            "bytearray" -> bundle.putByteArray(key, value.hexToByteArray())
            "charsequence" -> bundle.putCharSequence(key, value)
            "byte" -> bundle.putByte(key, parseByte(value))
            "float" -> bundle.putFloat(key, value.toFloat())
            else -> throw IllegalArgumentException("Unsupported reader mode extra type: $type")
        }
    }

    private fun parseByte(rawValue: String): Byte {
        return if (rawValue.startsWith("0x", ignoreCase = true)) {
            rawValue.substring(2).toInt(16).toByte()
        } else {
            rawValue.toByte()
        }
    }
}
