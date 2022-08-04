package com.lokile.encrypter

import java.nio.ByteBuffer

internal fun Int.to4ByteArray() = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

internal fun ByteArray.toIntFrom4Bytes(): Int {
    if (this.size < Int.SIZE_BYTES) {
        throw Exception("Invalid byte length")
    }
    return if (this.size > Int.SIZE_BYTES) {
        ByteBuffer.wrap(copyOfRange(0, Int.SIZE_BYTES)).int
    } else {
        ByteBuffer.wrap(this).int
    }
}
