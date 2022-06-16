package com.lokile.dataencrypter.encrypters.imp

import android.content.Context
import android.os.Build
import com.lokile.dataencrypter.encrypters.EncryptedData
import com.lokile.dataencrypter.encrypters.IByteEncrypter
import com.lokile.dataencrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class ByteEncrypterImp : IByteEncrypter {

    private var keyProvider: ISecretKeyProvider
    private val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
    private lateinit var secretKey: Key

    constructor(
        keyProvider: ISecretKeyProvider
    ) {
        this.keyProvider = keyProvider
        loadKey()
    }

    constructor(
        context: Context,
        alias: String
    ) {
        this.keyProvider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AESSecretKeyProvider(alias)
        } else {
            RSASecretKeyProvider(context, alias)
        }
        loadKey()
    }

    private fun loadKey() {
        secretKey =
            keyProvider.getSecretKey() ?: throw Exception("Error when loading the secretKey")
    }

    override fun encrypt(data: ByteArray, iv: ByteArray?): EncryptedData? {
        try {
            if (iv != null) {
                cipher.init(
                    Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv)
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

    override fun decrypt(data: ByteArray): ByteArray? {
        val ivSize = data[0].toInt()
        return decrypt(
            EncryptedData(
                Arrays.copyOfRange(data, ivSize + 1, data.size),
                Arrays.copyOfRange(data, 1, ivSize + 1)
            )
        )
    }

    override fun resetKeys() {
        keyProvider.removeSecretKey()
        loadKey()
    }
}