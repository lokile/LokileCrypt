package com.lokile.lokiledataencrypter

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lokile.lokiledataencrypter.encrypters.IByteEncrypter
import com.lokile.lokiledataencrypter.encrypters.imp.ByteEncrypterImp
import com.lokile.lokiledataencrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.lokiledataencrypter.secretKeyProviders.imp.PasswordSecretKeyProvider
import com.lokile.lokiledataencrypter.secretKeyProviders.imp.RSASecretKeyProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ByteEncrypterImpTest {
    var encrypters = mutableListOf<IByteEncrypter>()

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
                ByteEncrypterImp(AESSecretKeyProvider("p1"))
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            encrypters.add(
                ByteEncrypterImp(RSASecretKeyProvider(context = appContext, "p2"))
            )
        }
        encrypters.add(
            ByteEncrypterImp(PasswordSecretKeyProvider(appContext, "p3", "demoPassword"))
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
            encrypter.resetKeys()

            val decrypted2 = encrypter.decrypt(encrypted)
            assertNull(decrypted2)

            assertFalse(
                encrypter.encrypt(source.toByteArray()).contentEquals(
                    encrypted
                )
            )
        }
    }
}