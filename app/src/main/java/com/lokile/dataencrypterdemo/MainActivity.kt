package com.lokile.dataencrypterdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.coroutineScope
import com.lokile.encrypter.Encrypter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Greeting(
                    name = "Android",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        demoByteEncrypter()
    }

    fun demoByteEncrypter() {
        lifecycle.coroutineScope.launch {
            val e = Encrypter("demo")
            val source = "demo123"
            val encrypted = e.encryptOrNull(source.toByteArray())
            if (encrypted != null) {
                val decrypted = e.decryptOrNull(encrypted)
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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting("Android")
}