package com.kormax.universalreader.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

inline fun <reified T : Enum<T>> EnumNameVariantSerializer(
    crossinline fromString: (String) -> T
): KSerializer<T> {
    return object : KSerializer<T> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor(T::class.simpleName!!, PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): T {
            val value =
                try {
                    decoder.decodeString().replace("_", "").replace("-", "").lowercase()
                } catch (e: Exception) {
                    null
                }
            return fromString(value ?: "")
        }

        override fun serialize(encoder: Encoder, value: T) {
            encoder.encodeString(value.name)
        }
    }
}
