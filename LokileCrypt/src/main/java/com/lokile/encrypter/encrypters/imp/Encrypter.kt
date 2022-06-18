package com.lokile.encrypter.encrypters.imp

import android.content.Context
import android.util.Base64
import com.lokile.encrypter.encrypters.EncryptedData
import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import java.util.*

class Encrypter : BaseEncrypter {

    constructor(
        context: Context,
        keyProvider: ISecretKeyProvider,
        algorithm: String = "AES/CBC/PKCS7PADDING"
    ) : super(context, keyProvider, algorithm)

    constructor(
        context: Context,
        alias: String,
        algorithm: String = "AES/CBC/PKCS7PADDING"
    ) : super(context, alias, algorithm)

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
}