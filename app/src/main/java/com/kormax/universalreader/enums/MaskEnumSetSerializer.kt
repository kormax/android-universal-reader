package com.kormax.universalreader.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

inline fun <reified T : Enum<T>> MaskEnumSetSerializer(
    crossinline encode: (Encoder, Set<T>) -> Unit,
    crossinline decode: (Decoder) -> Set<T>,
): KSerializer<Set<T>> {
    return object : KSerializer<Set<T>> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor(T::class.simpleName!!, PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Set<T> {
            return decode(decoder)
        }

        override fun serialize(encoder: Encoder, value: Set<T>) {
            encode(encoder, value)
        }
    }
}
