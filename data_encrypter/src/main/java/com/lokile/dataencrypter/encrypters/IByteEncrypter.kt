package com.lokile.dataencrypter.encrypters

import java.nio.ByteBuffer

data class EncryptedData(val data: ByteArray, val iv: ByteArray) {
    fun toByteArray(): ByteArray {
        return ByteBuffer.allocate(iv.size + data.size + 1)
            .apply {
                put(iv.size.toByte())
                put(iv)
                put(data)
            }.array()
    }
}

interface IByteEncrypter {
    fun encrypt(data: ByteArray, iv: ByteArray? = null): EncryptedData?
    fun decrypt(data: EncryptedData): ByteArray?
    fun decrypt(data: ByteArray): ByteArray?
    fun resetKeys()
}