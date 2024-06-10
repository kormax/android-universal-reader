package com.kormax.universalreader

import android.nfc.tech.IsoDep
import com.kormax.universalreader.apple.vas.VasReaderConfiguration
import com.kormax.universalreader.google.smarttap.SmartTapReaderConfiguration
import com.kormax.universalreader.iso7816.Iso7816Aid
import com.kormax.universalreader.iso7816.Iso7816Command
import com.kormax.universalreader.tlv.ber.BerTlvMessage

class ValueAddedServicesReaderConfiguration(
    val vas: VasReaderConfiguration?,
    val smartTap: SmartTapReaderConfiguration?
) {
    suspend fun read(
        isoDep: IsoDep,
        hook: (String, Any) -> Unit = { _, _ -> }
    ): ValueAddedServicesResult {
        val selectOseCommand = Iso7816Command.selectAid(Iso7816Aid.VAS)
        hook("command", selectOseCommand)
        val selectOseResponse = isoDep.transceive(selectOseCommand)
        hook("response", selectOseResponse)
        if (selectOseResponse.sw.toHexString() != "9000") {
            throw Exception("Could not select OSE.VAS applet")
        }

        val walletType =
            String(
                BerTlvMessage(selectOseResponse.data)
                    .findByTagElseThrow("6f")
                    .findByTagElseThrow("50")
                    .value
                    .toByteArray(),
                Charsets.UTF_8
            )

        hook("log", "Wallet type ${walletType}")

        return when (walletType) {
            "ApplePay" -> vas?.read(isoDep, selectOseResponse, hook) ?: ValueAddedServicesResult()
            "AndroidPay" ->
                smartTap?.read(isoDep, selectOseResponse, hook) ?: ValueAddedServicesResult()
            else -> ValueAddedServicesResult()
        }
    }
}
