package com.lokile.encrypter.secretKeyProviders

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import com.lokile.encrypter.encrypterImpl.KEY_STORE_NAME
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

private val keyStoreInstance
    get() = KeyStore.getInstance(KEY_STORE_NAME)
        .apply { load(null) }

internal fun loadSecretKey(alias: String): SecretKey? {
    val keyStore = keyStoreInstance
    if (!keyStore.containsAlias(alias)) return null
    return try {
        keyStore.getKey(alias, null) as SecretKey
    } catch (e: Exception) {
        e.printStackTrace()
        keyStore.deleteEntry(alias)
        null
    }
}

internal fun createNewSecretKey(alias: String): SecretKey? {
    return try {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_NAME)
            .apply {
                init(
                    KeyGenParameterSpec
                        .Builder(
                            alias,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setRandomizedEncryptionRequired(false)
                        .setKeySize(256)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build()
                )
            }.generateKey()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

internal fun saveExternalSecretKey(alias: String, key: ByteArray) {
    keyStoreInstance.setEntry(
        alias, KeyStore.SecretKeyEntry(SecretKeySpec(key, AES_ALGORITHM)),
        KeyProtection
            .Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()
    )
}

internal fun destroySecretKey(alias: String): Boolean {
    try {
        val keyStore = keyStoreInstance
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
            return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

internal fun containsSecretKey(alias: String) = keyStoreInstance.containsAlias(alias)

internal val ByteArray.asSecretKey get() = SecretKeySpec(this, AES_ALGORITHM)

internal val String.internalAlias get() = "alias$this"