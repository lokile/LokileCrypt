package com.lokile.dataencrypterdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.lokile.dataencrypter.encrypters.imp.Encrypter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        demoByteEncrypter()
    }

    fun demoByteEncrypter() {
        val e = Encrypter(this, "demo")
        val source = "demo123"
        val encrypted = e.encrypt(source.toByteArray())
        if (encrypted != null) {
            val decrypted = e.decrypt(encrypted)
            if (decrypted != null) {
                Log.d("AndroidUtils", "Successful, data=${String(decrypted)}")
            } else {
                Log.d("AndroidUtils", "Failed to decrypt")
            }
        } else {
            Log.d("AndroidUtils", "failed to encrypt")
        }
    }
}