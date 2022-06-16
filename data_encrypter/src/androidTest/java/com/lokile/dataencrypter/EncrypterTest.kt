package com.lokile.dataencrypter

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lokile.dataencrypter.encrypters.IEncrypter
import com.lokile.dataencrypter.encrypters.imp.Encrypter
import com.lokile.dataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.PasswordSecretKeyProvider
import com.lokile.dataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class EncrypterTest {
    var encrypters = mutableListOf<IEncrypter>()

    @After
    fun tearDown() {
        encrypters.forEach {
            it.resetKeys()
        }
    }

    @Before
    fun before() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            encrypters.add(
                Encrypter(AESSecretKeyProvider("p1"))
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            encrypters.add(
                Encrypter(RSASecretKeyProvider(context = appContext, "p2"))
            )
        }
        encrypters.add(
            Encrypter(PasswordSecretKeyProvider(appContext, "p3", "demoPassword"))
        )
    }

    @Test
    fun testEncrypters() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted = encrypter.encrypt(source.toByteArray())
            assertNotNull(encrypted)

            val decrypted = encrypter.decrypt(encrypted!!)
            assertNotNull(decrypted)

            assertEquals(source, String(decrypted!!))
            assertFalse(
                encrypter.encrypt(source.toByteArray())!!.toByteArray().contentEquals(
                    encrypted.toByteArray()
                )
            )

            encrypter.resetKeys()
            val decrypted2 = encrypter.decrypt(encrypted)

            assertNull(decrypted2)
        }
    }
}