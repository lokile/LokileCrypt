package com.lokile.encrypter.secretKeyProviders.imp

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.annotation.RequiresApi
import java.security.Key
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@RequiresApi(Build.VERSION_CODES.M)
internal class AESSecretKeyProvider(alias: String) :
    BaseSecretKeyProvider(alias) {

    override fun getSecretKey(): Key? {
        synchronized(this) {
            try {
                val keyStore = getKeyStore()
                if (keyStore.containsAlias(privateAlias)) {
                    try {
                        return keyStore.getKey(privateAlias, null) as SecretKey
                    } catch (e: Exception) {
                        e.printStackTrace()
                        keyStore.deleteEntry(privateAlias)
                    }
                }
                return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_NAME)
                    .apply {
                        init(
                            KeyGenParameterSpec
                                .Builder(
                                    privateAlias,
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

    override fun removeSecretKey(): Boolean {
        synchronized(this) {
            return removeKeyStoreAlias()
        }
    }

    override fun saveAesSecretKey(key: ByteArray) {
        val newKey = SecretKeySpec(key, "AES")
        val keystore = KeyStore.getInstance(KEY_STORE_NAME)
            .apply { load(null) }

        keystore.setEntry(
            privateAlias, KeyStore.SecretKeyEntry(newKey),
            KeyProtection
                .Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
        )
    }
}