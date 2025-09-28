package com.lokile.encrypter.encrypterImpl

import android.util.Base64
import java.nio.ByteBuffer
import java.util.Arrays

class EncryptedData(val data: ByteArray, val iv: ByteArray)

val String.asEncryptedData
    get() = try {
        Base64.decode(this, Base64.NO_WRAP).asEncryptedData
    } catch (_: Exception) {
        null
    }

val EncryptedData.asByteArray
    get(): ByteArray {
        return ByteBuffer.allocate(iv.size + data.size + 1)
            .apply {
                put(iv.size.toByte())
                put(iv)
                put(data)
            }.array()
    }


val ByteArray.asEncryptedData
    get(): EncryptedData {
        if (isEmpty()) {
            throw Exception("Data is empty")
        }

        val ivSize = this[0].toInt()
        return EncryptedData(
            Arrays.copyOfRange(this, ivSize + 1, this.size),
            Arrays.copyOfRange(this, 1, ivSize + 1)
        )
    }

val EncryptedData.asString: String
    get() = Base64.encodeToString(asByteArray, Base64.NO_WRAP)