package com.lokile.encrypter

import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lokile.encrypter.encrypters.IEncrypter
import com.lokile.encrypter.encrypters.imp.Encrypter
import com.lokile.encrypter.secretKeyProviders.SecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.PasswordSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.RSASecretKeyProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.Key
import javax.crypto.KeyGenerator


@RunWith(AndroidJUnit4::class)
class EncrypterTest {
    var encrypters = mutableListOf<IEncrypter>()
    lateinit var appContext: Context

    @After
    fun tearDown() {
        encrypters.forEach {
            it.resetKeys()
        }
    }

    @Before
    fun before() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            encrypters.add(
                Encrypter(appContext, AESSecretKeyProvider("p1"))
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            encrypters.add(
                Encrypter(appContext, RSASecretKeyProvider(context = appContext, "p2"))
            )
        }
        encrypters.add(
            Encrypter(
                appContext,
                PasswordSecretKeyProvider(appContext, "p3", "demoPassword")
            )
        )
        //test custom key
        encrypters.add(
            Encrypter(
                appContext,
                object : SecretKeyProvider() {
                    val aesKey by lazy {
                        KeyGenerator.getInstance("AES")
                            .apply {
                                init(256)
                            }.generateKey()
                    }

                    override fun getAlias(): String {
                        return "testAlias"
                    }

                    override fun getSecretKey(): Key? {
                        return aesKey
                    }

                    override fun getIv(): ByteArray? {
                        return null
                    }

                }
            )
        )
    }

    @Test
    fun testEncryptersInStringWithFixedIv() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted1 = encrypter.encrypt(source, false)
            assertNotNull(encrypted1)
            val decrypted1 = encrypter.decrypt(encrypted1!!)
            assertEquals(decrypted1, source)
            assertEquals(encrypter.encrypt(source, false), encrypted1)
        }
    }

    @Test
    fun testEncryptersInStringWithRandomIv() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted1 = encrypter.encrypt(source)
            assertNotNull(encrypted1)
            val decrypted1 = encrypter.decrypt(encrypted1!!)
            assertEquals(decrypted1, source)
            assertNotEquals(encrypter.encrypt(source), encrypted1)
        }
    }

    @Test
    fun testEncryptersInByteWithFixedIv() {
        encrypters.forEach {
            val source = "DemoText"
            val encrypted1 = it.encrypt(source.toByteArray(), false)
            assertNotNull(encrypted1)
            val decrypted1 = it.decrypt(encrypted1!!)
            val decrypted2 = it.decrypt(encrypted1.toByteArray())

            assertNotNull(decrypted1)
            assertNotNull(decrypted2)

            assertTrue(decrypted1.contentEquals(decrypted2))
            assertEquals(source, String(decrypted1!!))
            assertEquals(source, String(decrypted2!!))

            assertEquals(
                it.encrypt(source.toByteArray(), false)?.toStringData()!!,
                encrypted1.toStringData()
            )
        }
    }


    @Test
    fun testEncryptersInByteWithRandomIv() {
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

            assertNotEquals(
                it.encrypt(source.toByteArray())?.toStringData()!!,
                encrypted1.toStringData()
            )
        }
    }

    @Test
    fun testFixedIv() {
        var encrypter: Encrypter? = null
        var testValue = "testValue"
        try {
            encrypter = Encrypter(appContext, "testAlias")
            val encrypted1 = encrypter.encrypt(testValue, false)
            val encrypted11 = encrypter.encrypt(testValue, false)

            encrypter = Encrypter(appContext, "testAlias")
            val encrypted2 = encrypter.encrypt(testValue, false)
            val encrypted21 = encrypter.encrypt(testValue, false)

            assertEquals(encrypted1, encrypted11)
            assertEquals(encrypted2, encrypted21)
            assertEquals(encrypted1, encrypted2)
        } finally {
            encrypter?.resetKeys()
        }
    }

    @Test
    fun testFixedIvWithCustomKeyProvider() {
        val originalData = " Hello world!"
        val encrypter1 = Encrypter(appContext, object : SecretKeyProvider() {
            val aesKey by lazy {
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey()
            }

            override fun getAlias() = "testAlias1"

            override fun getSecretKey(): Key? {
                return aesKey
            }

            override fun getIv(): ByteArray? {
                return "12345678".toByteArray()
            }
        })
        val ed11 = encrypter1.encrypt(originalData)
        val ed12 = encrypter1.encrypt(originalData, false)
        val ed13 = encrypter1.encrypt(originalData, false)
        assertEquals(ed11, ed12)
        assertEquals(ed12, ed13)
        encrypter1.resetKeys()
        assertEquals(encrypter1.encrypt(originalData), ed11)


        val encrypter2 = Encrypter(appContext, object : SecretKeyProvider() {
            val aesKey by lazy {
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey()
            }

            override fun getAlias() = "testAlias2"

            override fun getSecretKey(): Key? {
                return aesKey
            }

            override fun getIv(): ByteArray? {
                return null
            }
        })
        val ed21 = encrypter2.encrypt(originalData)
        val ed22 = encrypter2.encrypt(originalData, false)
        val ed23 = encrypter2.encrypt(originalData, false)
        encrypter2.resetKeys()
        val ed24 = encrypter2.encrypt(originalData, false)
        assertNotEquals(ed21, ed22)
        assertEquals(ed22, ed23)
        assertEquals(ed23, ed24)

        val encrypter3 = Encrypter(appContext, object : SecretKeyProvider() {
            val aesKey by lazy {
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey()
            }

            override fun getAlias() = ""

            override fun getSecretKey(): Key? {
                return aesKey
            }

            override fun getIv(): ByteArray? {
                return null
            }
        })
        val ed31 = encrypter3.encrypt(originalData)
        val ed32 = encrypter3.encrypt(originalData)
        val ed33 = encrypter3.encrypt(originalData, false)
        assertNotNull(ed31)
        assertNotNull(ed32)
        assertNotEquals(ed31, ed32)
        assertNull(ed33)
    }

    @Test
    fun testDifferenceAlias() {
        var encrypter1: Encrypter? = null
        var encrypter2: Encrypter? = null
        var encrypter3: Encrypter? = null

        try {
            val data = "Hello World"
            encrypter1 = Encrypter(appContext, "alias1")
            encrypter2 = Encrypter(appContext, "alias1")
            encrypter3 = Encrypter(appContext, "alias3")
            val e1 = encrypter1.encrypt(data, false)
            val e2 = encrypter2.encrypt(data, false)
            val e3 = encrypter3.encrypt(data, false)
            assertEquals(e1, e2)
            assertNotEquals(e1, e3)
            assertNotEquals(e2, e3)
        } finally {
            encrypter3?.resetKeys()
            encrypter1?.resetKeys()
            encrypter2?.resetKeys()
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

            if (encrypter.resetKeys()) {
                val decrypted2 = encrypter.decrypt(encrypted)
                assertNull(decrypted2)
            }
        }
    }
}