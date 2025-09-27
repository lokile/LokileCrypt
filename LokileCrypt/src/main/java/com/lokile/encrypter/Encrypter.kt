package com.lokile.encrypter

import com.lokile.encrypter.encrypterImpl.EncryptedData
import com.lokile.encrypter.encrypterImpl.EncrypterImpl
import com.lokile.encrypter.secretKeyProviders.AESSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.asSecretKey
import com.lokile.encrypter.secretKeyProviders.containsSecretKey
import com.lokile.encrypter.secretKeyProviders.destroySecretKey
import com.lokile.encrypter.secretKeyProviders.internalAlias
import com.lokile.encrypter.secretKeyProviders.saveExternalSecretKey
import java.security.Key
import javax.crypto.KeyGenerator


interface Encrypter {

    suspend fun encryptOrNull(data: ByteArray): EncryptedData?
    suspend fun encryptOrNull(data: String): String?
    suspend fun encryptFile(
        inputPath: String,
        encryptedFilePath: String,
    ): Boolean

    suspend fun decryptFile(encryptedFilePath: String, outputFilePath: String): Boolean
    suspend fun decryptOrNull(data: String): String?
    suspend fun decryptOrNull(data: EncryptedData): ByteArray?
    suspend fun decryptOrNull(data: ByteArray, iv: ByteArray? = null): ByteArray?

    companion object {
        operator fun invoke(
            id: String,
            algorithm: String? = null
        ): Encrypter = EncrypterImpl(
            keyProvider = AESSecretKeyProvider(id.internalAlias),
            algorithm = algorithm
        )

        operator fun invoke(
            aesKey: Key,
            iv: ByteArray? = null,
            algorithm: String? = null
        ): Encrypter = EncrypterImpl(
            keyProvider = object : ISecretKeyProvider {
                override val secretKey = aesKey
                override val iv = iv
            },
            algorithm = algorithm
        )

        operator fun invoke(
            aesKey: ByteArray,
            iv: ByteArray? = null,
            algorithm: String? = null
        ): Encrypter = EncrypterImpl(
            keyProvider = object : ISecretKeyProvider {
                override val secretKey = aesKey.asSecretKey
                override val iv = iv
            },
            algorithm = algorithm
        )

        fun saveSecretKeyToDevice(aesKey: ByteArray, id: String) {
            saveExternalSecretKey(id.internalAlias, aesKey)
        }

        fun removeSecretKeyFromDevice(id: String) {
            destroySecretKey(id.internalAlias)
        }

        fun hasSecretKey(id: String): Boolean {
            return containsSecretKey(id.internalAlias)
        }

        fun newSecretKey(keySize: Int = 256): ByteArray {
            return KeyGenerator.getInstance("AES")
                .apply {
                    init(keySize)
                }.generateKey().encoded
        }
    }
}