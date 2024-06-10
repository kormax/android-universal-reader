package com.kormax.universalreader.enums

import com.kormax.universalreader.structable.Packable
import kotlin.enums.enumEntries

interface UByteEnum : Packable {
    val value: UByte

    override fun toUByteArray() = ubyteArrayOf(value)

    companion object {
        inline fun <reified T> fromName(name: String): T? where T : Enum<T>, T : UByteEnum {
            return enumEntries<T>().find {
                it.name.replace("_", "").replace("-", "") == name.replace("_", "").replace("-", "")
            }
        }
    }
}
