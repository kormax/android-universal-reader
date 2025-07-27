package com.kormax.universalreader.iso7816

interface Iso7816Target {
    suspend fun connect()

    suspend fun disconnect()

    suspend fun transceive(command: Iso7816Command): Iso7816Response

    suspend fun transceive(data: ByteArray): ByteArray =
        transceive(Iso7816Command.fromByteArray(data)).toByteArray()

    suspend fun transceive(data: UByteArray): UByteArray =
        transceive(Iso7816Command.fromUByteArray(data)).toUByteArray()
}
