package com.lokile.encrypter.encrypterImpl

import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

internal class CipherProvider(
    private val algorithm: String,
    private val keyProvider: ISecretKeyProvider
) {
    private val cipher by lazy { Cipher.getInstance(algorithm) }

    fun getEncryptCipher(): Cipher {
        val key = keyProvider.secretKey ?: error("Can't create encrypt key")
        val iv = keyProvider.iv
        if (iv != null) {
            cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                IvParameterSpec(iv)
            )
        } else {
            cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
            )
        }
        return cipher
    }

    fun getDecryptCipher(iv: ByteArray? = null): Cipher {
        val key = keyProvider.secretKey ?: error("Can't create encrypt key")
        val decryptIv = iv ?: keyProvider.iv ?: error("IV must not be null")
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            IvParameterSpec(decryptIv)
        )
        return cipher
    }
}