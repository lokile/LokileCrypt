package com.lokile.encrypter.secretKeyProviders

import java.security.Key

internal class AESSecretKeyProvider(private val alias: String) : ISecretKeyProvider {

    override val secretKey: Key? by lazy {
        synchronized(this) {
            loadSecretKey(alias)
                ?: createNewSecretKey(alias)
        }
    }

    override val iv: ByteArray? = null
}
