package com.lokile.dataencrypter.secretKeyProviders.imp

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import java.math.BigInteger
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

internal class RSASecretKeyProvider(context: Context, alias: String) :
    BaseSecretKeyProvider(alias) {
    private val PREF_NAME = "irYeZkBUkwKkBTFt9QFcfYfWdWMqQ"
    private val RSA_MODE = "RSA/ECB/PKCS1Padding"
    private val RSA_ALGORITHM_NAME = "RSA"
    private val prefAlias = "3TJ83RHqz7Xx" + alias
    private val app = context.applicationContext

    private fun wrapAesKey(key: Key): ByteArray {
        val keyStore = getKeyStore()
        val entry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.WRAP_MODE, entry.certificate.publicKey)
        return cipher.wrap(key)
    }

    private fun unwrapAesKey(key: ByteArray): Key {
        val keyStore = getKeyStore()
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(
            Cipher.UNWRAP_MODE,
            (keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry).privateKey
        )
        return cipher.unwrap(key, AES_ALGORITHM, Cipher.SECRET_KEY)
    }

    private fun getAesKey(): Key {
        val pref = app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val encryptKey = pref.getString(prefAlias, null)
        if (!encryptKey.isNullOrEmpty()) {
            val key = unwrapAesKey(
                Base64.decode(encryptKey, Base64.DEFAULT)
            )
            return key
        }

        val aesKey = createNewAesKey()
        pref.edit {
            putString(
                prefAlias,
                wrapAesKey(aesKey)
                    .let { Base64.encodeToString(it, Base64.DEFAULT) }
            )
        }
        return aesKey
    }

    @Suppress("DEPRECATION")
    override fun getSecretKey(): Key? {
        synchronized(this) {
            val keyStore = getKeyStore()
            if (keyStore.containsAlias(alias)) {
                try {
                    return getAesKey()
                } catch (e: Exception) {
                    e.printStackTrace()
                    removeSecretKey()
                }
            }
            val currentLocale = Locale.getDefault()
            try {
                Locale.setDefault(Locale.US)
                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 60)
                val spec = android.security.KeyPairGeneratorSpec.Builder(app)
                    .setAlias(alias)
                    .setSubject(X500Principal("CN=$alias"))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()
                val kpq = KeyPairGenerator.getInstance(RSA_ALGORITHM_NAME, KEY_STORE_NAME)
                kpq.initialize(spec)
                kpq.generateKeyPair()
                return getAesKey()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                Locale.setDefault(currentLocale)
            }
            return null
        }
    }

    override fun removeSecretKey(): Boolean {
        synchronized(this) {
            try {
                app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit { remove(prefAlias) }
                return removeKeyStoreAlias()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }
}