package com.lokile.lokiledataencrypter.encrypters

interface IByteEncrypter {
    fun encrypt(data: ByteArray): ByteArray?
    fun decrypt(data: ByteArray): ByteArray?
    fun resetKeys()
}