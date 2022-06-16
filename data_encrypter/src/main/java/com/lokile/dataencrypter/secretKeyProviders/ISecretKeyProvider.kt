package com.lokile.dataencrypter.secretKeyProviders

import java.security.Key

interface ISecretKeyProvider {
    fun getSecretKey(): Key?
    fun removeSecretKey(): Boolean
}