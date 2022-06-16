package com.lokile.dataencrypter.secretKeyProviders.imp

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.Key
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
internal class AESSecretKeyProvider(alias: String) :
    BaseSecretKeyProvider(alias) {

    override fun getSecretKey(): Key? {
        synchronized(this) {
            try {
                val keyStore = getKeyStore()
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

    override fun removeSecretKey(): Boolean {
        synchronized(this) {
            return removeKeyStoreAlias()
        }
    }
}