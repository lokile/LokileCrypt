package com.lokile.encrypter.encrypters.imp

import android.content.Context
import android.util.Base64
import androidx.annotation.VisibleForTesting
import com.lokile.encrypter.encrypters.EncryptedData
import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import java.security.Key
import java.util.*

class Encrypter constructor(context: Context, alias: String) :
    BaseEncrypter(context, alias) {

    override fun encrypt(data: ByteArray, useRandomizeIv: Boolean): EncryptedData? {
        return super.encrypt(data, useRandomizeIv)
    }

    override fun decrypt(data: EncryptedData): ByteArray? {
        return super.decrypt(data)
    }

    override fun encrypt(data: String, useRandomizeIv: Boolean): String? {
        return encrypt(data.toByteArray(), useRandomizeIv)?.toStringData()
    }

    override fun decrypt(data: ByteArray): ByteArray? {
        if (data.isEmpty()) {
            return null
        }
        val ivSize = data[0].toInt()
        return decrypt(
            EncryptedData(
                Arrays.copyOfRange(data, ivSize + 1, data.size),
                Arrays.copyOfRange(data, 1, ivSize + 1)
            )
        )
    }

    override fun decrypt(data: String): String? {
        return decrypt(Base64.decode(data, Base64.DEFAULT))?.let { String(it) }
    }

    class Builder(context: Context, alias: String) {
        private val encrypter by lazy { Encrypter(context, alias) }

        internal fun setSecretKeyProvider(provider: ISecretKeyProvider): Builder {
            encrypter.keyProvider = provider
            return this
        }

        fun setSecretKey(key: Key, iv: ByteArray? = null): Builder {
            encrypter.keyProvider = object : ISecretKeyProvider {
                override fun getSecretKey(): Key {
                    return key
                }

                override fun getIv(): ByteArray? {
                    return iv
                }

                override fun removeSecretKey(): Boolean {
                    return false
                }

            }
            return this
        }

        fun setEncryptAlgorithm(algorithm: String): Builder {
            encrypter.algorithm = algorithm
            return this
        }

        fun build() = encrypter
    }
}