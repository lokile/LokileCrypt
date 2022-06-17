package com.lokile.dataencrypter.encrypters.imp

import android.content.Context
import android.os.Build
import android.util.Base64
import com.lokile.dataencrypter.encrypters.EncryptedData
import com.lokile.dataencrypter.encrypters.IEncrypter
import com.lokile.dataencrypter.secretKeyProviders.ISecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class Encrypter : BaseEncrypter {

    constructor(
        context: Context,
        keyProvider: ISecretKeyProvider,
        useFixedIv: Boolean
    ) : super(context, keyProvider, useFixedIv)

    constructor(
        context: Context,
        alias: String,
        useFixedIv: Boolean
    ) : super(context, alias, useFixedIv)

    override fun encrypt(data: ByteArray): EncryptedData? {
        return super.encrypt(data)
    }

    override fun decrypt(data: EncryptedData): ByteArray? {
        return super.decrypt(data)
    }

    override fun encrypt(data: String): String? {
        return encrypt(data.toByteArray())?.toStringData()
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

    override fun decrypt(data: String): String? {
        return decrypt(Base64.decode(data, Base64.DEFAULT))?.let { String(it) }
    }
}