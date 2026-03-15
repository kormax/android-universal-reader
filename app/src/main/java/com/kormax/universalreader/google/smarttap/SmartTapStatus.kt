package com.kormax.universalreader.google.smarttap

import com.kormax.universalreader.iso7816.Iso7816Response
import com.kormax.universalreader.structable.Packable

sealed class SmartTapStatus(val sw1: UByte, val sw2: UByte) : Packable {
    sealed class Success(sw1: UByte, sw2: UByte) : SmartTapStatus(sw1, sw2)

    sealed class Error(sw1: UByte, sw2: UByte) : SmartTapStatus(sw1, sw2)

    // 0x90xx
    sealed class SuccessFinal(sw2: UByte) : Success(0x90U, sw2)

    // Request completed successfully with normal terminal authentication.
    object Ok : SuccessFinal(0x00U)

    // Request completed, but no transferable service payload was available.
    object OkNoPayload : SuccessFinal(0x01U)

    // Negotiation accepted pre-signed authentication instead of live auth.
    object OkPreSignedAuth : SuccessFinal(0x02U)

    // 0x91xx
    sealed class SuccessNonFinal(sw2: UByte) : Success(0x91U, sw2)

    // Partial payload returned; terminal should continue with GET MORE.
    object OkMorePayload : SuccessNonFinal(0x00U)

    // 0x92xx
    sealed class TransientFailure(sw2: UByte) : Error(0x92U, sw2)

    // Cryptographic operation failed (key/signature/encryption/decryption flow).
    object CryptoFailure : TransientFailure(0x01U)

    // Command sequencing/state was inconsistent during data chunk exchange.
    object ExecutionFailure : TransientFailure(0x03U)

    class UnknownTransientFailure(val statusCode: UByte) : TransientFailure(statusCode)

    // 0x93xx
    sealed class UserActionNeeded(sw2: UByte) : SmartTapStatus(0x93U, sw2)

    // Device must be unlocked or lock setup must be completed before continuing.
    object DeviceLocked : UserActionNeeded(0x00U)

    // User selection UI was shown and terminal should retry after selection.
    object DisambiguationScreenShown : UserActionNeeded(0x02U)

    class UnknownUserActionNeeded(val statusCode: UByte) : UserActionNeeded(statusCode)

    // 0x94xx
    sealed class TerminalError(sw2: UByte) : Error(0x94U, sw2)

    // Request rate or collector-ID constraints were violated in the active window.
    object TooManyRequests : TerminalError(0x06U)

    // Terminal sent an unsupported/unknown APDU command for this applet state.
    object UnknownTerminalCommand : TerminalError(0x00U)

    // APDU or nested NDEF payload could not be parsed or lacked required fields.
    object ParsingFailure : TerminalError(0x02U)

    // Negotiation cryptography parameters were missing, malformed, or invalid.
    object InvalidCryptoInput : TerminalError(0x03U)

    // GET MORE was requested when no chunked response context existed.
    object RequestMoreNotApplicable : TerminalError(0x04U)

    // GET MORE was requested after all pending chunks were already sent.
    object MoreDataNotAvailable : TerminalError(0x05U)

    // Session reached processing without required merchant/collector identity.
    object NoMerchantSet : TerminalError(0x07U)

    // Provided pushback URI was invalid or did not meet URI requirements.
    object InvalidPushbackUri : TerminalError(0x08U)

    class UnknownTerminalError(val statusCode: UByte) : TerminalError(statusCode)

    // 0x95xx
    sealed class PermanentError(sw2: UByte) : Error(0x95U, sw2)

    // Terminal authentication failed (bad signature, missing key, or unknown auth state).
    object AuthFailed : PermanentError(0x00U)

    // Requested protocol version was outside supported or consistent range.
    object VersionNotSupported : PermanentError(0x02U)

    class UnknownPermanentError(val statusCode: UByte) : PermanentError(statusCode)

    class Unknown(val status: UShort) :
        Error(sw1 = (status.toUInt() shr 8).toUByte(), sw2 = status.toUByte())

    override fun toUByteArray() = ubyteArrayOf(sw1, sw2)

    override fun toString() = "${javaClass.simpleName}(${hex(sw1)}, ${hex(sw2)})"

    val isSuccess: Boolean
        get() = this is Success

    val isTransientError: Boolean
        get() = this is TransientFailure

    val isError: Boolean
        get() = this is Error

    companion object {
        private fun hex(value: UByte): String =
            "0x${value.toString(16).padStart(2, '0').uppercase()}"

        val entries: List<SmartTapStatus> =
            listOf(
                Ok,
                OkNoPayload,
                OkPreSignedAuth,
                OkMorePayload,
                CryptoFailure,
                ExecutionFailure,
                DeviceLocked,
                DisambiguationScreenShown,
                TooManyRequests,
                UnknownTerminalCommand,
                ParsingFailure,
                InvalidCryptoInput,
                RequestMoreNotApplicable,
                MoreDataNotAvailable,
                NoMerchantSet,
                InvalidPushbackUri,
                AuthFailed,
                VersionNotSupported,
            )

        private val bySW: Map<UInt, SmartTapStatus> =
            entries.associateBy { (it.sw1.toUInt() shl 8) or it.sw2.toUInt() }

        fun from(sw1: UByte, sw2: UByte): SmartTapStatus {
            bySW[(sw1.toUInt() shl 8) or sw2.toUInt()]?.let {
                return it
            }

            return when (sw1) {
                0x92U.toUByte() -> UnknownTransientFailure(sw2)
                0x93U.toUByte() -> UnknownUserActionNeeded(sw2)
                0x94U.toUByte() -> UnknownTerminalError(sw2)
                0x95U.toUByte() -> UnknownPermanentError(sw2)
                else -> Unknown(((sw1.toUInt() shl 8) or sw2.toUInt()).toUShort())
            }
        }

        fun from(response: Iso7816Response): SmartTapStatus = from(response.sw1, response.sw2)
    }
}
