package com.lokile.encrypter.secretKeyProviders.imp

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec

internal class PasswordSecretKeyProvider(context: Context, alias: String, val password: String) :
    BaseSecretKeyProvider(alias) {

    private val FILE_NAME = "bhbXGcQxuZ6EmjxjmzchjBtmHQRk8"
    private val app = context.applicationContext
    private fun getCustomKeyStore(): KeyStore? {
        val keystoreFile = File(app.filesDir, FILE_NAME)
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        if (keystoreFile.exists()) {
            try {
                FileInputStream(keystoreFile.absoluteFile).use {
                    keyStore.load(
                        it, password.toCharArray()
                    )
                }
                return keyStore
            } catch (e: Exception) {
                e.printStackTrace()
                keystoreFile.delete()
            }
        }
        return try {
            FileOutputStream(keystoreFile.absolutePath).use {
                keyStore.load(null, null)
                keyStore.store(it, password.toCharArray())
            }
            keyStore
        } catch (e: Exception) {
            null
        }
    }

    private fun saveKeyStore(keyStore: KeyStore) {
        try {
            val keyStoreFile = File(app.filesDir, FILE_NAME)
            FileOutputStream(keyStoreFile.absolutePath).use {
                keyStore.store(it, password.toCharArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getSecretKey(): Key? {
        synchronized(this) {
            val keyStore = getCustomKeyStore() ?: return null
            if (keyStore.containsAlias(privateAlias)) {
                try {
                    val entry = keyStore.getEntry(
                        privateAlias,
                        KeyStore.PasswordProtection(privateAlias.toCharArray())
                    ) as KeyStore.SecretKeyEntry

                    return entry.secretKey
                } catch (e: Exception) {
                    e.printStackTrace()
                    removeSecretKey()
                }
            }
            try {
                val entry = KeyStore.SecretKeyEntry(createNewAesKey())
                keyStore.setEntry(
                    privateAlias,
                    entry,
                    KeyStore.PasswordProtection(privateAlias.toCharArray())
                )
                saveKeyStore(keyStore)
                return entry.secretKey
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    override fun removeSecretKey(): Boolean {
        synchronized(this) {
            try {
                val keyStore = getCustomKeyStore() ?: return false
                if (keyStore.containsAlias(privateAlias)) {
                    keyStore.deleteEntry(privateAlias)
                    saveKeyStore(keyStore)
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }

    override fun saveAesSecretKey(key: ByteArray) {
        val keystore = getCustomKeyStore() ?: return
        keystore.setEntry(
            privateAlias,
            KeyStore.SecretKeyEntry(SecretKeySpec(key, AES_ALGORITHM)),
            KeyStore.PasswordProtection(privateAlias.toCharArray())
        )
        saveKeyStore(keystore)
    }
}