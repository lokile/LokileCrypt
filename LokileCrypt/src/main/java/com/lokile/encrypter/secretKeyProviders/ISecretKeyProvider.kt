package com.lokile.encrypter.secretKeyProviders

import java.security.Key

interface ISecretKeyProvider {
    fun getAlias():String
    fun getSecretKey(): Key?
    fun getIv(): ByteArray?
    fun removeSecretKey(): Boolean
}