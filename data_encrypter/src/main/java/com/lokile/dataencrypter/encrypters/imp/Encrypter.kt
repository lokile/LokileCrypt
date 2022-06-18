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
    ) : super(context, keyProvider)

    constructor(
        context: Context,
        alias: String,
    ) : super(context, alias)

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