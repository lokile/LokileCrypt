package com.lokile.encrypter.secretKeyProviders

import android.content.Context
import android.os.Build
import com.lokile.encrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.RSASecretKeyProvider
import java.util.*


fun Context.saveAesKeyToKeyStore(key: ByteArray, alias: String) {
    val newAlias = "alias$alias"
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            AESSecretKeyProvider(newAlias).saveAesSecretKey(key)
        }
        else -> {
            RSASecretKeyProvider(this, newAlias).saveAesSecretKey(key)
        }
    }

}

fun getRandomAesKey(keySize: Int): ByteArray {
    val randomKeyBytes = ByteArray(keySize / 8)
    val random = Random()
    random.nextBytes(randomKeyBytes)
    return randomKeyBytes
}