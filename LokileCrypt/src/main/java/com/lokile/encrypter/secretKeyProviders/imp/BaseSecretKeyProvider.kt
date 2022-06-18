package com.lokile.encrypter.secretKeyProviders.imp

import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal abstract class BaseSecretKeyProvider(val privateAlias: String) : ISecretKeyProvider {
    protected val KEY_SIZE = 256
    protected val AES_ALGORITHM = "AES"
    protected val KEY_STORE_NAME = "AndroidKeyStore"

    override fun getAlias(): String {
        return privateAlias
    }

    protected fun createNewAesKey(): SecretKey {
        return KeyGenerator.getInstance(AES_ALGORITHM)
            .apply { init(KEY_SIZE) }
            .generateKey()
    }

    protected fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(KEY_STORE_NAME)
            .apply { load(null) }
    }

    protected fun removeKeyStoreAlias(): Boolean {
        try {
            val keyStore = getKeyStore()
            if (keyStore.containsAlias(privateAlias)) {
                keyStore.deleteEntry(privateAlias)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun getIv(): ByteArray? {
        return null
    }
}