package com.lokile.encrypter.encrypterImpl

import java.nio.ByteBuffer
import java.security.KeyStore


internal const val KEY_STORE_NAME = "AndroidKeyStore"


internal val Int.asBytes get() = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

internal val ByteArray.asInt
    get(): Int {
        if (this.size < Int.SIZE_BYTES) {
            throw Exception("Invalid byte length")
        }
        return if (this.size > Int.SIZE_BYTES) {
            ByteBuffer.wrap(copyOfRange(0, Int.SIZE_BYTES)).int
        } else {
            ByteBuffer.wrap(this).int
        }
    }