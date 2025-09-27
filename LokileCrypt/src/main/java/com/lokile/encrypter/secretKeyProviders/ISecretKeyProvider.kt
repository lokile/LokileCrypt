package com.lokile.encrypter.secretKeyProviders

import java.security.Key

internal interface ISecretKeyProvider {
    val secretKey: Key?
    val iv: ByteArray?
}