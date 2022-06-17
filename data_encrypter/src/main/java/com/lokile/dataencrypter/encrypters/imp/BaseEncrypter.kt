package com.lokile.dataencrypter.encrypters.imp

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.core.content.edit
import com.lokile.dataencrypter.encrypters.EncryptedData
import com.lokile.dataencrypter.encrypters.IEncrypter
import com.lokile.dataencrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import java.nio.ByteBuffer
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

abstract class BaseEncrypter : IEncrypter {
    protected val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
    protected var keyProvider: ISecretKeyProvider
    protected var app: Context
    protected var iv: ByteArray? = null
    protected lateinit var secretKey: Key
    private val prefName = "xfhw9LYsjwSaR4cfAakQhVFn1"
    private val ivKey = "352VZrcCFprRHWZ8cg9DvytRb"

    constructor(context: Context, keyProvider: ISecretKeyProvider) {
        app = context.applicationContext
        this.keyProvider = keyProvider
        loadKey()
    }

    constructor(context: Context, alias: String) {
        app = context.applicationContext
        this.keyProvider = loadDefaultKeyProvider(alias)
        loadKey()
    }

    protected fun loadDefaultKeyProvider(alias: String): ISecretKeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AESSecretKeyProvider(alias)
        } else {
            RSASecretKeyProvider(app, alias)
        }
    }

    protected fun loadKey() {
        secretKey =
            keyProvider.getSecretKey() ?: throw Exception("Error when loading the secretKey")
    }

    private fun saveIv(iv: ByteArray) {
        app.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .edit {
                putString(ivKey + keyProvider.getAlias(), Base64.encodeToString(iv, Base64.DEFAULT))
            }
    }

    private fun loadIv(): ByteArray? {
        val pref = app.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val encryptedIv = pref.getString(ivKey + keyProvider.getAlias(), null)
        if (encryptedIv != null) {
            return decrypt(encryptedIv)?.toByteArray().apply {
                if (this == null) {
                    pref.edit {
                        remove(ivKey + keyProvider.getAlias())
                    }
                }
            }
        } else {
            return null
        }
    }

    override fun encrypt(data: ByteArray, useFixedIv: Boolean): EncryptedData? {
        try {
            var encryptIv = this.keyProvider.getIv()
            if (encryptIv == null && useFixedIv) {
                if (iv == null) {
                    iv = loadIv()
                }
                encryptIv = iv
            }
            if (encryptIv != null) {
                cipher.init(
                    Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(encryptIv)
                )
            } else {
                cipher.init(
                    Cipher.ENCRYPT_MODE, secretKey
                )
            }
            val output = cipher.doFinal(data)
            val iv = cipher.iv

            if (keyProvider.getIv() == null && useFixedIv && this.iv == null) {
                this.iv = iv
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val ivData = cipher.doFinal(iv)
                val ivIv = cipher.iv
                saveIv(
                    ByteBuffer.allocate(ivIv.size + ivData.size + 1)
                        .apply {
                            put(ivIv.size.toByte())
                            put(ivIv)
                            put(ivData)
                        }.array()
                )
            }
            return EncryptedData(output, iv)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun decrypt(data: EncryptedData): ByteArray? {
        try {
            cipher.init(
                Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(data.iv)
            )
            return cipher.doFinal(data.data)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun resetKeys() {
        keyProvider.removeSecretKey()
        app.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit {
            remove(ivKey + keyProvider.getAlias())
        }
        loadKey()
    }
}