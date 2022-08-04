package com.lokile.encrypter.encrypters.imp

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.lokile.encrypter.encrypters.EncryptedData
import com.lokile.encrypter.encrypters.toEncryptedData
import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.RSASecretKeyProvider
import com.lokile.encrypter.to4ByteArray
import com.lokile.encrypter.toIntFrom4Bytes
import java.io.File
import java.nio.ByteBuffer
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

abstract class BaseEncrypter(context: Context, val alias: String) {
    protected var algorithm: String = "AES/CBC/PKCS7PADDING"
    protected val cipher by lazy { Cipher.getInstance(algorithm) }
    protected var keyProvider: ISecretKeyProvider? = null
    protected var app = context.applicationContext
    protected var fixedIv: ByteArray? = null
    protected lateinit var secretKey: Key
    private val prefName = "xfhw9LYsjwSaR4cfAakQhVFn1"
    private val savedIvKeyWord = "352VZrcCFprRHWZ8cg9DvytRb$alias"

    private fun getOrCreateKeyProvider(): ISecretKeyProvider {
        return this.keyProvider ?: loadDefaultKeyProvider("alias${alias}")
    }

    protected fun initCipher(mode: Int, iv: ByteArray? = null) {
        if (!this::secretKey.isInitialized) {
            loadKey()
        }
        if (iv != null) {
            cipher.init(
                mode, secretKey, IvParameterSpec(iv)
            )
        } else {
            cipher.init(
                mode, secretKey
            )
        }
    }

    protected fun loadDefaultKeyProvider(alias: String): ISecretKeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AESSecretKeyProvider(alias)
        } else {
            RSASecretKeyProvider(app, alias)
        }
    }

    protected fun loadKey() {
        val key = getOrCreateKeyProvider().getSecretKey()
        if (key != null) {
            secretKey = key
        } else {
            throw Exception("Error when loading the secretKey")
        }
    }

    protected fun saveFixedIv(iv: ByteArray, useRandomizeIv: Boolean) {
        if (this.getOrCreateKeyProvider()
                .getIv() != null || useRandomizeIv || this.fixedIv != null
        ) {
            return
        }

        this.fixedIv = iv
        val ivEncrypted = encryptData(iv, true)
        app.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .edit {
                putString(
                    savedIvKeyWord,
                    ivEncrypted.toStringData()
                )
            }
    }

    protected fun loadFixedIv(useRandomizeIv: Boolean): ByteArray? {
        val encryptIv = this.getOrCreateKeyProvider().getIv()
        if (encryptIv != null || useRandomizeIv) {
            return encryptIv
        }
        if (this.fixedIv != null) {
            return this.fixedIv
        }

        val pref = app.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val encryptedIv = pref.getString(savedIvKeyWord, null)
        if (encryptedIv != null) {
            return try {
                decryptData(encryptedIv.toEncryptedData())
            } catch (e: Exception) {
                e.printStackTrace()
                pref.edit {
                    remove(savedIvKeyWord)
                }
                null
            }
        } else {
            return null
        }
    }

    fun encryptFile(
        inputPath: String,
        encryptedOutputFile: String,
        useRandomizeIv: Boolean
    ): Boolean {
        synchronized(this) {
            val inputFile = File(inputPath)
            val outputFile = File(encryptedOutputFile)
            if (inputFile.absolutePath == outputFile.absolutePath) {
                return false
            }
            if (!inputFile.exists()) {
                return false
            }
            try {
                inputFile.inputStream().use { input ->
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(1024 * 8)
                        var bytes = input.read(buffer)
                        initCipher(Cipher.ENCRYPT_MODE, loadFixedIv(useRandomizeIv))
                        output.write(
                            ByteBuffer.allocate(cipher.iv.size + 1)
                                .apply {
                                    put(cipher.iv.size.toByte())
                                    put(cipher.iv)
                                }.array()
                        )
                        while (bytes >= 0) {
                            val inputBytes = if (bytes == buffer.size) {
                                buffer
                            } else {
                                buffer.copyOfRange(0, bytes)
                            }

                            val encryptedBytes = if (input.available() > 0) {
                                cipher.update(inputBytes)
                            } else {
                                cipher.doFinal(inputBytes)
                            }

                            output.write(
                                ByteBuffer.allocate(Int.SIZE_BYTES + encryptedBytes.size)
                                    .apply {
                                        put(encryptedBytes.size.to4ByteArray())
                                        put(encryptedBytes)
                                    }.array()
                            )
                            bytes = input.read(buffer)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }
    }

    fun decryptFile(encryptedFilePath: String, outputFilePath: String): Boolean {
        synchronized(this) {
            val inputFile = File(encryptedFilePath)
            val outputFile = File(outputFilePath)
            if (!inputFile.exists()) {
                return false
            }
            try {
                inputFile.inputStream().use { input ->
                    outputFile.outputStream().use { output ->
                        var buffer = ByteArray(1024 * 8)
                        var bytesLength: Int

                        input.read(buffer, 0, 1)
                        val ivSize = buffer[0].toInt()
                        input.read(buffer, 0, ivSize)

                        initCipher(Cipher.DECRYPT_MODE, buffer.copyOfRange(0, ivSize))

                        while (true) {
                            bytesLength = input.read(buffer, 0, Int.SIZE_BYTES)
                            if (bytesLength <= 0) {
                                return true
                            }

                            val dataSize = buffer.toIntFrom4Bytes()
                            if (buffer.size < dataSize) {
                                buffer = ByteArray(dataSize)
                            }
                            bytesLength = input.read(buffer, 0, dataSize)
                            if (bytesLength <= 0) {
                                return false
                            }

                            val originBytes = if (input.available() > 0) {
                                cipher.update(buffer.copyOfRange(0, dataSize))
                            } else {
                                cipher.doFinal(buffer.copyOfRange(0, dataSize))
                            }
                            output.write(originBytes)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }

    protected fun encryptData(data: ByteArray, useRandomizeIv: Boolean): EncryptedData {
        synchronized(this) {
            initCipher(Cipher.ENCRYPT_MODE, loadFixedIv(useRandomizeIv))

            val output = cipher.doFinal(data)
            val iv = cipher.iv

            saveFixedIv(iv, useRandomizeIv)
            return EncryptedData(output, iv)
        }
    }

    protected fun decryptData(data: EncryptedData): ByteArray {
        synchronized(this) {
            initCipher(Cipher.DECRYPT_MODE, data.iv)
            return cipher.doFinal(data.data)
        }
    }

    fun resetKeys(): Boolean {
        synchronized(this) {
            try {
                app.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit {
                    remove(savedIvKeyWord)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    app.deleteSharedPreferences(prefName)
                }
                fixedIv = null
                return keyProvider?.removeSecretKey() ?: false
            } finally {
                loadKey()
            }
        }
    }
}