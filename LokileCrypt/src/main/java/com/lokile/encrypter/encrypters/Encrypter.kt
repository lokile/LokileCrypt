package com.lokile.encrypter.encrypters

import com.lokile.encrypter.AES_ALGORITHM
import com.lokile.encrypter.EncryptedData
import com.lokile.encrypter.AESSecretKeyProvider
import com.lokile.encrypter.ISecretKeyProvider
import java.security.Key
import com.lokile.encrypter.stringData
import com.lokile.encrypter.toEncryptedData
import javax.crypto.spec.SecretKeySpec


interface Encrypter {

    fun encryptOrNull(data: ByteArray): EncryptedData?
    fun encryptOrNull(data: String): String?
    fun encryptFile(
        inputPath: String,
        encryptedFilePath: String,
    ): Boolean

    fun decryptFile(encryptedFilePath: String, outputFilePath: String): Boolean
    fun decryptOrNull(data: String): String?
    fun decryptOrNull(data: EncryptedData): ByteArray?
    fun decryptOrNull(data: ByteArray, iv: ByteArray? = null): ByteArray?

    companion object {
        operator fun invoke(alias: String) = Builder(alias).build()
    }

    class Builder(private val alias: String) {
        private var keyProvider: ISecretKeyProvider? = null
        private var algorithm: String? = null

        fun setSecretKey(aesKey: Key, iv: ByteArray? = null): Builder {
            keyProvider = object : ISecretKeyProvider {
                override fun getOrCreateKey() = aesKey
                override fun getIv() = iv
            }
            return this
        }

        fun setSecretKey(aesKey: ByteArray, iv: ByteArray? = null): Builder {
            setSecretKey(SecretKeySpec(aesKey, AES_ALGORITHM), iv)
            return this
        }

        fun setEncryptAlgorithm(algorithm: String): Builder {
            this.algorithm = algorithm
            return this
        }

        fun build() = object : Encrypter {
            val baseEncrypter = BaseEncrypter(
                keyProvider = keyProvider ?: AESSecretKeyProvider("alias${alias}"),
                algorithm = algorithm
            )

            override fun encryptOrNull(data: ByteArray): EncryptedData? {
                return try {
                    baseEncrypter.encryptData(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            override fun encryptOrNull(data: String): String? {
                return try {
                    baseEncrypter.encryptData(data.toByteArray()).stringData
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            override fun encryptFile(
                inputPath: String,
                encryptedFilePath: String
            ): Boolean {
                return baseEncrypter.encryptFile(inputPath, encryptedFilePath)
            }

            override fun decryptFile(
                encryptedFilePath: String,
                outputFilePath: String
            ): Boolean {
                return baseEncrypter.decryptFile(
                    encryptedFilePath,
                    outputFilePath
                )
            }

            override fun decryptOrNull(data: ByteArray, iv: ByteArray?): ByteArray? {
                return try {
                    if (iv != null) {
                        baseEncrypter.decryptData(EncryptedData(data, iv))
                    } else {
                        baseEncrypter.decryptData(data.toEncryptedData())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            override fun decryptOrNull(data: String): String? {
                return try {
                    String(baseEncrypter.decryptData(data.toEncryptedData()))
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            override fun decryptOrNull(data: EncryptedData): ByteArray? {
                return try {
                    baseEncrypter.decryptData(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

        }
    }
}