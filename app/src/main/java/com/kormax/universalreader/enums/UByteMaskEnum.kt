package com.kormax.universalreader.enums

import kotlin.enums.enumEntries
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface UByteMaskEnum : UByteEnum {
    companion object {
        inline fun <reified T> fromName(name: String): T? where T : Enum<T>, T : UByteMaskEnum {
            return enumEntries<T>().find {
                it.name.replace("_", "").replace("-", "").lowercase() ==
                    name.replace("_", "").replace("-", "").lowercase()
            }
        }

        inline fun <reified T> fromNames(names: Array<String>): Set<T> where
        T : Enum<T>,
        T : UByteMaskEnum {
            val res = names.mapNotNull { name -> fromName<T>(name) }.sortedBy { it.value }.toSet()
            return res
        }

        inline fun <reified T> fromMask(mask: UByte): Set<T> where T : Enum<T>, T : UByteMaskEnum {
            return enumValues<T>()
                .filter { (mask and it.value) != 0U.toUByte() }
                .sortedBy { it.value }
                .toSet()
        }

        inline fun <reified T> deserializeToSet(decoder: Decoder): Set<T> where
        T : Enum<T>,
        T : UByteMaskEnum {
            try {
                return fromNames(
                    decoder
                        .decodeSerializableValue(ListSerializer(String.serializer()))
                        .toTypedArray())
            } catch (_: Exception) {}
            try {
                return fromMask<T>(decoder.decodeByte().toUByte())
            } catch (_: Exception) {}

            return emptySet()
        }

        inline fun <reified T> serializeToSet(encoder: Encoder, value: Set<T>) where
        T : Enum<T>,
        T : UByteMaskEnum {
            encoder.encodeSerializableValue(
                ListSerializer(String.serializer()), value.toList().map { it.name.lowercase() })
        }
    }
}
