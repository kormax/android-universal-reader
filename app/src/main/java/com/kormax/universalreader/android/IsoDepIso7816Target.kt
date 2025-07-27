package com.kormax.universalreader.android

import android.nfc.tech.IsoDep
import com.kormax.universalreader.iso7816.Iso7816Command
import com.kormax.universalreader.iso7816.Iso7816Response
import com.kormax.universalreader.iso7816.Iso7816Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IsoDepIso7816Target(private val isoDep: IsoDep) : Iso7816Target {
    override suspend fun connect() = withContext(Dispatchers.IO) { isoDep.connect() }

    override suspend fun disconnect() = withContext(Dispatchers.IO) { isoDep.close() }

    override suspend fun transceive(command: Iso7816Command): Iso7816Response =
        withContext(Dispatchers.IO) {
            return@withContext Iso7816Response.fromByteArray(
                isoDep.transceive(command.toByteArray())
            )
        }
}
