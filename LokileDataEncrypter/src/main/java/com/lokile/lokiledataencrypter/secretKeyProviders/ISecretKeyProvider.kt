package com.lokile.lokiledataencrypter.secretKeyProviders

import java.security.Key

interface ISecretKeyProvider {
    fun getSecretKey(): Key?
    fun removeSecretKey(): Boolean
}