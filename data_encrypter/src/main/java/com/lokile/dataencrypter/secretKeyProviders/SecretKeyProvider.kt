package com.lokile.dataencrypter.secretKeyProviders

abstract class SecretKeyProvider : ISecretKeyProvider {
    override fun getAlias(): String {
        return ""
    }

    override fun removeSecretKey(): Boolean {
        return false
    }
}