package com.lokile.lokiledataencrypter.encrypters.imp

import android.content.Context
import android.os.Build
import com.lokile.lokiledataencrypter.encrypters.IByteEncrypter
import com.lokile.lokiledataencrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.lokiledataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.lokiledataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import java.nio.ByteBuffer
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

    override fun encrypt(data: ByteArray): ByteArray? {
        try {
            cipher.init(
                Cipher.ENCRYPT_MODE, secretKey
            )
            val output = cipher.doFinal(data)
            return ByteBuffer.allocate(cipher.iv.size + output.size + 1)
                .apply {
                    put(cipher.iv.size.toByte())
                    put(cipher.iv)
                    put(output)
                }.array()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun decrypt(data: ByteArray): ByteArray? {
        return try {
            val ivSize = data[0].toInt()
            val encryptedData = Arrays.copyOfRange(data, ivSize + 1, data.size)
            val ivSpec = IvParameterSpec(
                Arrays.copyOfRange(data, 1, ivSize + 1)
            )
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun resetKeys() {
        keyProvider.removeSecretKey()
        loadKey()
    }
}