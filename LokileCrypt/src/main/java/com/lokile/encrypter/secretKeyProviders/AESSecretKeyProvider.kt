package com.lokile.encrypter

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import com.lokile.encrypter.AES_ALGORITHM
import com.lokile.encrypter.KEY_SIZE
import com.lokile.encrypter.KEY_STORE_NAME
import com.lokile.encrypter.keyStore
import com.lokile.encrypter.removeKeyStoreAlias
import java.security.Key
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal class AESSecretKeyProvider(private val alias: String) : ISecretKeyProvider {

    override fun getOrCreateKey(): Key? {
        synchronized(this) {
            try {
                val keyStore = keyStore
                if (keyStore.containsAlias(alias)) {
                    try {
                        return keyStore.getKey(alias, null) as SecretKey
                    } catch (e: Exception) {
                        e.printStackTrace()
                        keyStore.deleteEntry(alias)
                    }
                }
                return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_NAME)
                    .apply {
                        init(
                            KeyGenParameterSpec
                                .Builder(
                                    alias,
                                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                                )
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                .setRandomizedEncryptionRequired(false)
                                .setKeySize(KEY_SIZE)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                .build()
                        )
                    }.generateKey()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    override fun clearKey(): Boolean {
        synchronized(this) {
            return removeKeyStoreAlias(alias = alias)
        }
    }

    override fun saveKey(key: ByteArray) {
        val newKey = SecretKeySpec(key, AES_ALGORITHM)
        val keystore = KeyStore.getInstance(KEY_STORE_NAME)
            .apply { load(null) }

        keystore.setEntry(
            alias, KeyStore.SecretKeyEntry(newKey),
            KeyProtection
                .Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
        )
    }
}