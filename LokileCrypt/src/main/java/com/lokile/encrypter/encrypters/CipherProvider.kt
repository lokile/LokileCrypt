package com.lokile.encrypter.encrypters

import com.lokile.encrypter.ISecretKeyProvider
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

internal class CipherProvider(
    private val algorithm: String,
    private val keyProvider: ISecretKeyProvider
) {
    private val cipher by lazy { Cipher.getInstance(algorithm) }

    fun getEncryptCipher(): Cipher {
        val key = keyProvider.getOrCreateKey() ?: error("Can't create encrypt key")
        val iv = keyProvider.getIv()
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
        val key = keyProvider.getOrCreateKey() ?: error("Can't create encrypt key")
        val decryptIv = iv ?: keyProvider.getIv() ?: error("IV must not be null")
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            IvParameterSpec(decryptIv)
        )
        return cipher
    }
}