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
                Encrypter(appContext, AESSecretKeyProvider("p1"), false)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            encrypters.add(
                Encrypter(appContext, RSASecretKeyProvider(context = appContext, "p2"), false)
            )
        }
        encrypters.add(
            Encrypter(appContext, PasswordSecretKeyProvider(appContext, "p3", "demoPassword"), false)
        )
    }

    @Test
    fun testEncryptersInString() {
        encrypters.forEach {encrypter->
            val source = "DemoText"
            val encrypted1 = encrypter.encrypt(source)
            assertNotNull(encrypted1)
            val decrypted1 = encrypter.decrypt(encrypted1!!)
            assertEquals(decrypted1, source)
        }
    }

    @Test
    fun testEncryptersInByte() {
        encrypters.forEach {
            val source = "DemoText"
            val encrypted1 = it.encrypt(source.toByteArray())
            assertNotNull(encrypted1)
            val decrypted1 = it.decrypt(encrypted1!!)
            val decrypted2 = it.decrypt(encrypted1.toByteArray())

            assertNotNull(decrypted1)
            assertNotNull(decrypted2)

            assertTrue(decrypted1.contentEquals(decrypted2))
            assertEquals(source, String(decrypted1!!))
            assertEquals(source, String(decrypted2!!))
        }
    }
    @Test
    fun testResetKey() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted = encrypter.encrypt(source)
            assertNotNull(encrypted)

            val decrypted = encrypter.decrypt(encrypted!!)
            assertNotNull(decrypted)

            assertEquals(source, decrypted)
            assertNotEquals(encrypter.encrypt(source), encrypted)

            encrypter.resetKeys()
            val decrypted2 = encrypter.decrypt(encrypted)
            assertNull(decrypted2)
        }
    }
}