package com.lokile.encrypter.encrypters

import android.util.Base64
import java.nio.ByteBuffer
import java.util.*
import javax.crypto.Cipher

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

fun String.toEncryptedData() = Base64.decode(this, Base64.DEFAULT).toEncryptedData()

data class EncryptedData(val data: ByteArray, val iv: ByteArray) {
    fun toByteArray(): ByteArray {
        return ByteBuffer.allocate(iv.size + data.size + 1)
            .apply {
                put(iv.size.toByte())
                put(iv)
                put(data)
            }.array()
    }

    fun toStringData() = Base64.encodeToString(toByteArray(), Base64.DEFAULT)
}

interface IEncrypter {
    fun encrypt(data: ByteArray, useRandomizeIv: Boolean = true): EncryptedData
    fun encrypt(data: String, useRandomizeIv: Boolean = true): String

    fun decrypt(data: EncryptedData): ByteArray
    fun decrypt(data: ByteArray): ByteArray
    fun decrypt(data: String): String

    fun encryptOrNull(data: ByteArray, useRandomizeIv: Boolean = true): EncryptedData?
    fun encryptOrNull(data: String, useRandomizeIv: Boolean = true): String?

    fun decryptOrNull(data: ByteArray): ByteArray?
    fun decryptOrNull(data: String): String?
    fun decryptOrNull(data: EncryptedData): ByteArray?

    fun getEncryptCipher(useRandomizeIv: Boolean = true): Cipher
    fun getDecryptCipher(iv: ByteArray): Cipher
    fun resetKeys(): Boolean
}