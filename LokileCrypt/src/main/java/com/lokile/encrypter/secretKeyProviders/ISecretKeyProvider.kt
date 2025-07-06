package com.lokile.encrypter

import java.security.Key

internal interface ISecretKeyProvider {
    fun getOrCreateKey(): Key?
    fun getIv(): ByteArray? = null
    fun clearKey(): Boolean = false
    fun saveKey(key: ByteArray) {}
}