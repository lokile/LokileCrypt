package com.lokile.encrypter.encrypters.imp

import android.content.Context
import com.lokile.encrypter.encrypters.EncryptedData
import com.lokile.encrypter.encrypters.IEncrypter
import com.lokile.encrypter.encrypters.toEncryptedData
import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.encrypter.to4ByteArray
import com.lokile.encrypter.toIntFrom4Bytes
import java.io.File
import java.nio.ByteBuffer
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Encrypter constructor(context: Context, alias: String) :
    BaseEncrypter(context, alias), IEncrypter {

    override fun encrypt(data: ByteArray, useRandomizeIv: Boolean): EncryptedData {
        return encryptData(data, useRandomizeIv)
    }

    override fun decrypt(data: EncryptedData): ByteArray {
        return decryptData(data)
    }

    override fun encrypt(data: String, useRandomizeIv: Boolean): String {
        return encryptData(data.toByteArray(), useRandomizeIv).toStringData()
    }

    override fun decrypt(data: ByteArray): ByteArray {
        return decryptData(data.toEncryptedData())
    }

    override fun decrypt(data: String): String {
        return String(decryptData(data.toEncryptedData()))
    }

    override fun decrypt(data: ByteArray, iv: ByteArray): ByteArray {
        return decryptData(EncryptedData(data, iv))
    }

    override fun encryptOrNull(data: ByteArray, useRandomizeIv: Boolean): EncryptedData? {
        return try {
            encrypt(data, useRandomizeIv)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun encryptOrNull(data: String, useRandomizeIv: Boolean): String? {
        return try {
            encrypt(data, useRandomizeIv)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun decryptOrNull(data: ByteArray): ByteArray? {
        return try {
            decrypt(data)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun decryptOrNull(data: String): String? {
        return try {
            decrypt(data)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun decryptOrNull(data: EncryptedData): ByteArray? {
        return try {
            decrypt(data)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun decryptOrNull(data: ByteArray, iv: ByteArray): ByteArray? {
        return try {
            decryptData(EncryptedData(data, iv))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getEncryptCipher(useRandomizeIv: Boolean): Cipher {
        initCipher(Cipher.ENCRYPT_MODE, loadFixedIv(useRandomizeIv))
        return cipher
    }

    override fun getDecryptCipher(iv: ByteArray): Cipher {
        initCipher(Cipher.DECRYPT_MODE, iv)
        return cipher
    }

    class Builder(context: Context, alias: String) {
        private val encrypter by lazy { Encrypter(context, alias) }

        internal fun setSecretKeyProvider(provider: ISecretKeyProvider): Builder {
            encrypter.keyProvider = provider
            return this
        }

        private fun setSecretKeyProvider(aesKey: Key, iv: ByteArray? = null) {
            encrypter.keyProvider = object : ISecretKeyProvider {
                override fun getSecretKey(): Key {
                    return aesKey
                }

                override fun getIv(): ByteArray? {
                    return iv
                }

                override fun removeSecretKey(): Boolean {
                    return false
                }

                override fun saveAesSecretKey(key: ByteArray) {

                }

            }
        }

        fun setSecretKey(aesKey: ByteArray, iv: ByteArray): Builder {
            setSecretKey(SecretKeySpec(aesKey, "AES"), iv)
            return this
        }

        fun setSecretKey(aesKey: ByteArray): Builder {
            setSecretKey(SecretKeySpec(aesKey, "AES"))
            return this
        }

        fun setSecretKey(aesKey: Key, iv: ByteArray): Builder {
            setSecretKeyProvider(aesKey, iv)
            return this
        }

        fun setSecretKey(aesKey: Key): Builder {
            setSecretKeyProvider(aesKey)
            return this
        }

        fun setEncryptAlgorithm(algorithm: String): Builder {
            encrypter.algorithm = algorithm
            return this
        }

        fun build() = encrypter
    }
}