package com.lokile.encrypter.secretKeyProviders

import android.content.Context
import com.lokile.encrypter.AESSecretKeyProvider
import com.lokile.encrypter.encrypters.Encrypter
import com.lokile.encrypter.keyStore
import java.util.Random


fun Encrypter.Companion.saveAesKeyToDevice(key: ByteArray, alias: String) {
    val newAlias = "alias$alias"
    AESSecretKeyProvider(newAlias).saveKey(key)
}

fun Encrypter.Companion.removeAesKeyFromDevice(alias: String) {
    val newAlias = "alias$alias"
    AESSecretKeyProvider(newAlias).clearKey()
}

fun Encrypter.Companion.getRandomAesKey(keySize: Int): ByteArray {
    val randomKeyBytes = ByteArray(keySize / 8)
    val random = Random()
    random.nextBytes(randomKeyBytes)
    return randomKeyBytes
}

fun Encrypter.Companion.hasSecretKey(alias: String): Boolean {
    return keyStore.containsAlias("alias$alias")
}