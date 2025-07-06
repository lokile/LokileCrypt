package com.lokile.encrypter

import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import java.util.Arrays


internal val KEY_STORE_NAME = "AndroidKeyStore"
internal val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
internal val KEY_SIZE = 256
internal val keyStore
    get() = KeyStore.getInstance(KEY_STORE_NAME)
        .apply { load(null) }

internal fun removeKeyStoreAlias(alias: String): Boolean {
    try {
        val keyStore = keyStore
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
            return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

internal fun Int.toByteArray() = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

internal fun ByteArray.toInt(): Int {
    if (this.size < Int.SIZE_BYTES) {
        throw Exception("Invalid byte length")
    }
    return if (this.size > Int.SIZE_BYTES) {
        ByteBuffer.wrap(copyOfRange(0, Int.SIZE_BYTES)).int
    } else {
        ByteBuffer.wrap(this).int
    }
}


fun ByteArray.toEncryptedData(): EncryptedData {
    if (isEmpty()) {
        throw Exception("Data is empty")
    }

    val ivSize = this[0].toInt()
    return EncryptedData(
        Arrays.copyOfRange(this, ivSize + 1, this.size),
        Arrays.copyOfRange(this, 1, ivSize + 1)
    )
}

class EncryptedData(val data: ByteArray, val iv: ByteArray)

fun String.toEncryptedData() = Base64.decode(this, Base64.NO_WRAP).toEncryptedData()
val EncryptedData.byteArray
    get(): ByteArray {
        return ByteBuffer.allocate(iv.size + data.size + 1)
            .apply {
                put(iv.size.toByte())
                put(iv)
                put(data)
            }.array()
    }

val EncryptedData.stringData get() = Base64.encodeToString(byteArray, Base64.NO_WRAP)
