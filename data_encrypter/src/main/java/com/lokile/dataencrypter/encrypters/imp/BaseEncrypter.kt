package com.lokile.dataencrypter.encrypters.imp

import android.content.Context
import android.os.Build
import com.lokile.dataencrypter.encrypters.EncryptedData
import com.lokile.dataencrypter.encrypters.IEncrypter
import com.lokile.dataencrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

abstract class BaseEncrypter : IEncrypter {
    protected val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
    protected var keyProvider: ISecretKeyProvider
    protected var app: Context
    protected var useFixedIv: Boolean
    protected lateinit var secretKey: Key

    constructor(context: Context, keyProvider: ISecretKeyProvider, useFixedIv: Boolean) {
        app = context.applicationContext
        this.useFixedIv = useFixedIv
        this.keyProvider = keyProvider
        loadKey()
    }

    constructor(context: Context, alias: String, useFixedIv: Boolean) {
        app = context.applicationContext
        this.useFixedIv = useFixedIv
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

    override fun encrypt(data: ByteArray): EncryptedData? {
        try {
            if (this.keyProvider.getIv() != null) {
                cipher.init(
                    Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(this.keyProvider.getIv())
                )
            } else {
                cipher.init(
                    Cipher.ENCRYPT_MODE, secretKey
                )
            }
            return EncryptedData(cipher.doFinal(data), cipher.iv)
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
        loadKey()
    }
}