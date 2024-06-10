package com.kormax.universalreader

import android.content.Context
import android.net.Uri
import android.nfc.tech.IsoDep
import com.kormax.universalreader.iso7816.Iso7816Command
import com.kormax.universalreader.iso7816.Iso7816Response
import com.kormax.universalreader.structable.Packable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.ECPointUtil
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.StringReader
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.MessageDigest
import java.security.Security
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec
import java.util.zip.Inflater
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Constants {
    companion object {
        const val NFC_LOG_MESSAGE = "com.kormax.universalreader.NFC_LOG_MESSAGE"
        const val VAS_READ_MESSAGE = "com.kormax.universalreader.VAlUE_ADDED_SERVICES_READ_RESULT"
    }
}

const val EC_PRIVATE_KEY_HEADER = "-----BEGIN EC PRIVATE KEY-----"
const val EC_PRIVATE_KEY_FOOTER = "-----END EC PRIVATE KEY-----"


fun UByteArray.toShort() =
    reversed().foldIndexed(0.toShort()) { index, acc, byte ->
        (acc + ((byte.toInt() shl index * 8).toShort())).toShort()
    }

fun UByteArray.toUShort() = toShort().toUShort()

fun UByteArray.toInt() =
    reversed().foldIndexed(0) { index, acc, byte -> acc + (byte.toInt() shl index * 8) }

fun UByteArray.toUInt() = toInt().toUInt()

fun UByteArray.toLong() =
    reversed().foldIndexed(0L) { index, acc, byte -> acc + (byte.toLong() shl index * 8) }

fun UByteArray.toULong() = toLong().toULong()

fun Char.isPrintable() = this in ' '..'~'

fun String.sha256() = hashString(this, "SHA-256").hexToUByteArray()

fun String.isHexFormat() = all { it in '0'..'9' || it in 'a'..'f' }

fun String.isPrintable() = all { it.isPrintable() }


fun ByteArray.sha256(): UByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(this)
    return md.digest().toUByteArray()
}

fun UByteArray.sha256(): UByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(this.toByteArray())
    return md.digest().toUByteArray()
}

fun ULong.toUByteArray(): UByteArray {
    return ubyteArrayOf(
        (this shr 56).toUByte() and 0xFFU,
        (this shr 48).toUByte() and 0xFFU,
        (this shr 40).toUByte() and 0xFFU,
        (this shr 32).toUByte() and 0xFFU,
        (this shr 24).toUByte() and 0xFFU,
        (this shr 16).toUByte() and 0xFFU,
        (this shr 8).toUByte() and 0xFFU,
        (this.toUByte() and 0xFFU),
    )
}

fun UInt.toUByteArray(): UByteArray {
    return ubyteArrayOf(
        (this shr 24).toUByte() and 0xFFU,
        (this shr 16).toUByte() and 0xFFU,
        (this shr 8).toUByte() and 0xFFU,
        (this.toUByte() and 0xFFU),
    )
}

fun UShort.toUByteArray(): UByteArray {
    return ubyteArrayOf(
        (this.toUInt() shr 8).toUByte(),
        (this.toUByte() and 0xFFU),
    )
}

fun UByteArray.decodeToString() = toByteArray().decodeToString()

fun Boolean.toUInt() = if (this) 1u else 0u

fun Boolean.toUByte(): UByte = if (this) 1u else 0u

fun IsoDep.transceive(data: Iso7816Command) =
    Iso7816Response.fromByteArray(transceive(data.toByteArray()))

fun IsoDep.transceive(data: Packable) = transceive(data.toByteArray()).toUByteArray()

fun IsoDep.transceive(data: UByteArray) = transceive(data.toByteArray()).toUByteArray()

private fun hashString(input: String, algorithm: String): String {
    return MessageDigest.getInstance(algorithm).digest(input.toByteArray()).fold("") { str, it ->
        str + "%02x".format(it)
    }
}

fun getEcPublicKeyFromUBytes(publicKey: UByteArray): ECPublicKey {
    val spec = ECNamedCurveTable.getParameterSpec("secp256r1")
    val kf = KeyFactory.getInstance("EC", BouncyCastleProvider())
    val params = ECNamedCurveSpec("secp256r1", spec.curve, spec.g, spec.n)
    val point = ECPointUtil.decodePoint(params.curve, publicKey.toByteArray())
    val pubKeySpec = ECPublicKeySpec(point, params)
    return kf.generatePublic(pubKeySpec) as ECPublicKey
}

fun getUBytesFromEcPublicKey(publicKey: ECPublicKey): UByteArray {
    val x: ByteArray = publicKey.w.affineX.toByteArray()
    val y: ByteArray = publicKey.w.affineY.toByteArray()
    val xbi = BigInteger(1, x)
    val ybi = BigInteger(1, y)
    val x9 = org.bouncycastle.asn1.x9.ECNamedCurveTable.getByName("secp256r1")
    val curve = x9.curve
    val point = curve.createPoint(xbi, ybi)
    return point.getEncoded(true).toUByteArray()
}

fun decompress(compressedData: ByteArray): ByteArray {
    val inflater = Inflater()
    inflater.setInput(compressedData)

    val outputStream = ByteArrayOutputStream(compressedData.size)
    val buffer = ByteArray(1024)
    try {
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        return outputStream.toByteArray()
    } catch (e: Exception) {
        throw IOException("Failed to decompress data: ${e.message}")
    } finally {
        outputStream.close()
        inflater.end()
    }
}

object UByteArraySerializer : KSerializer<UByteArray?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UByteArrayDeserializer", PrimitiveKind.STRING)

    @OptIn(ExperimentalEncodingApi::class)
    override fun deserialize(decoder: Decoder): UByteArray? {
        val input = decoder.decodeString()
        try {
            return Base64.decode(input).toUByteArray()
        } catch (e: IllegalArgumentException) {
            try {
                return input.hexToUByteArray()
            } catch (e: IllegalArgumentException) {
                return null
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: UByteArray?) {
        if (value.isNullOrEmpty()) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.toHexString())
        }
    }
}

object ECKeyPairSerializer : KSerializer<KeyPair?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ECKeyPairSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): KeyPair? {
        return loadECKeyFromString(decoder.decodeString())
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: KeyPair?) {
        encoder.encodeNull()
    }
}


@OptIn(ExperimentalEncodingApi::class)
fun loadECKeyFromString(pemString: String?): KeyPair? {
    Security.addProvider(BouncyCastleProvider())

    if (pemString == null) {
        return null
    }

    try {
        // Check if the input contains PEM headers
        val initialData =
            if (pemString.contains(EC_PRIVATE_KEY_HEADER)) {
                // Extract base64 data from PEM format
                val startIdx = pemString.indexOf(EC_PRIVATE_KEY_HEADER)
                val endIdx = pemString.indexOf(EC_PRIVATE_KEY_FOOTER)
                pemString
                    .substring(startIdx, endIdx)
                    .replace(EC_PRIVATE_KEY_HEADER, "")
                    .replace(EC_PRIVATE_KEY_FOOTER, "")
                    .replace("\n", "")
                    .trim()
            } else {
                pemString.replace("\n", "").trim()
            }

        val decodedData =
            if (initialData.isHexFormat()) {
                initialData.hexToByteArray()
            } else {
                Base64.decode(initialData)
            }

        // Convert decoded data to KeyPair
        val converter = JcaPEMKeyConverter()
        return converter.getKeyPair(
            PEMParser(
                    StringReader(
                        EC_PRIVATE_KEY_HEADER +
                            "\n" +
                            Base64.encode(decodedData) +
                            "\n" +
                            EC_PRIVATE_KEY_FOOTER
                    )
                )
                .readObject() as org.bouncycastle.openssl.PEMKeyPair
        )
    } catch (e: Exception) {}
    return null
}

fun loadJsonFile(context: Context, uri: Uri): String? {
    return try {
        // Open an InputStream for the selected file
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

        // Read file contents as a string
        return inputStream?.bufferedReader().use { it?.readText() }
    } catch (e: Exception) {
        // Handle any exceptions, such as file not found or deserialization error
        e.printStackTrace()
        null
    }
}
