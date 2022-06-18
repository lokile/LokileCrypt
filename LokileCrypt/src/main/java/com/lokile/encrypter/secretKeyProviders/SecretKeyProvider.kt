package com.lokile.encrypter.secretKeyProviders

abstract class SecretKeyProvider : ISecretKeyProvider {

    override fun removeSecretKey(): Boolean {
        return false
    }
}