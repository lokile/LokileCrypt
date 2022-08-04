package com.lokile.encrypter

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lokile.encrypter.encrypters.IEncrypter
import com.lokile.encrypter.encrypters.imp.Encrypter
import com.lokile.encrypter.secretKeyProviders.getRandomAesKey
import com.lokile.encrypter.secretKeyProviders.hasSecretKey
import com.lokile.encrypter.secretKeyProviders.imp.AESSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.PasswordSecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.imp.RSASecretKeyProvider
import com.lokile.encrypter.secretKeyProviders.removeAesKeyFromKeyStore
import com.lokile.encrypter.secretKeyProviders.saveAesKeyToKeyStore
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
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
                Encrypter.Builder(appContext, "p1")
                    .setSecretKeyProvider(AESSecretKeyProvider("p1"))
                    .build()
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            encrypters.add(
                Encrypter.Builder(appContext, "p2")
                    .setSecretKeyProvider(RSASecretKeyProvider(context = appContext, "p2"))
                    .build()
            )
        }
        encrypters.add(
            Encrypter.Builder(appContext, "p3")
                .setSecretKeyProvider(PasswordSecretKeyProvider(appContext, "p3", "demoPassword"))
                .build()
        )
        //test custom key
        encrypters.add(
            Encrypter.Builder(appContext, "p4")
                .setSecretKey(
                    KeyGenerator.getInstance("AES")
                        .apply {
                            init(256)
                        }.generateKey()
                )
                .build()
        )
        encrypters.add(
            Encrypter.Builder(appContext, "p5")
                .setSecretKey(
                    KeyGenerator.getInstance("AES")
                        .apply {
                            init(256)
                        }.generateKey().encoded
                )
                .build()
        )
    }

    @Test
    fun testEncryptersInStringWithFixedIv() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted1 = encrypter.encrypt(source, false)
            val decrypted1 = encrypter.decrypt(encrypted1)
            assertEquals(decrypted1, source)
            assertEquals(encrypter.encrypt(source, false), encrypted1)
        }
    }

    @Test
    fun testEncryptersInStringWithRandomIv() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted1 = encrypter.encrypt(source)
            val decrypted1 = encrypter.decrypt(encrypted1)
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
            val decrypted1 = it.decrypt(encrypted1)
            val decrypted2 = it.decrypt(encrypted1.toByteArray())
            assertTrue(decrypted1.contentEquals(decrypted2))
            assertEquals(source, String(decrypted1))
            assertEquals(source, String(decrypted2))

            assertEquals(
                it.encrypt(source.toByteArray(), false).toStringData(),
                encrypted1.toStringData()
            )
        }
    }

    @Test
    fun testSaveAesKey_feature() {
        val customKey = getRandomAesKey(256)
        appContext.saveAesKeyToKeyStore(customKey, "p6-0")
        assertTrue(hasSecretKey("p6-0"))
        var encrypter1: Encrypter? = null
        var encrypter2: Encrypter? = null
        try {
            val data = "DemoText"
            encrypter1 = Encrypter.Builder(appContext, "p6-0")
                .build()

            encrypter2 = Encrypter.Builder(appContext, "p6-1")
                .setSecretKey(customKey)
                .build()

            val en1 = encrypter1.encrypt(data)
            val de2 = encrypter2.decryptOrNull(en1)
            assertEquals(data, de2)
        } finally {
            encrypter1?.resetKeys()
            encrypter2?.resetKeys()
        }
    }

    @Test
    fun testSaveAndRemoveAesKey() {
        val newKey = getRandomAesKey(256)
        appContext.saveAesKeyToKeyStore(newKey, "key1")
        assertTrue(hasSecretKey("key1"))
        appContext.removeAesKeyFromKeyStore("key1")
        assertFalse(hasSecretKey("key1"))
    }

    @Test
    fun testEncryptersInByteWithRandomIv() {
        encrypters.forEach {
            val source = "DemoText"
            val encrypted1 = it.encrypt(source.toByteArray())
            assertNotNull(encrypted1)
            val decrypted1 = it.decrypt(encrypted1)
            val decrypted2 = it.decrypt(encrypted1.toByteArray())

            assertTrue(decrypted1.contentEquals(decrypted2))
            assertEquals(source, String(decrypted1))
            assertEquals(source, String(decrypted2))

            assertNotEquals(
                it.encrypt(source.toByteArray()).toStringData(),
                encrypted1.toStringData()
            )
        }
    }

    @Test
    fun testFixedIv() {
        var encrypter: Encrypter? = null
        val testValue = "testValue"
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
    fun testFixed_IV_WithCustomKeyProvider() {
        val originalData = " Hello world!"
        val encrypter1 = Encrypter
            .Builder(appContext, "testAlias1")
            .setSecretKey(
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey(),
                "1234567812345678".toByteArray()
            )
            .build()
        val ed11 = encrypter1.encrypt(originalData)
        val ed12 = encrypter1.encrypt(originalData, false)
        val ed13 = encrypter1.encrypt(originalData, false)
        assertEquals(ed11, ed12)
        assertEquals(ed12, ed13)
        encrypter1.resetKeys()
        assertEquals(encrypter1.encrypt(originalData), ed11)

        val encrypter2 = Encrypter.Builder(appContext, "testAlias2")
            .setSecretKey(
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey()
            )
            .build()
        val ed21 = encrypter2.encrypt(originalData)
        val ed22 = encrypter2.encrypt(originalData, false)
        val ed23 = encrypter2.encrypt(originalData, false)
        encrypter2.resetKeys()
        val ed24 = encrypter2.encrypt(originalData, false)
        assertNotEquals(ed21, ed22)
        assertEquals(ed22, ed23)
        assertNotEquals(ed23, ed24)

        val encrypter3 = Encrypter.Builder(appContext, "testAlias3")
            .setSecretKey(
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey().encoded
            )
            .build()
        val ed31 = encrypter3.encrypt(originalData)
        val ed32 = encrypter3.encrypt(originalData)
        val ed33 = encrypter3.encrypt(originalData, false)
        val ed34 = encrypter3.encrypt(originalData, false)
        assertNotEquals(ed31, ed32)
        assertEquals(ed33, ed34)

        val encrypter4 = Encrypter.Builder(appContext, "testAlias4")
            .setSecretKey(
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey().encoded,
                "1234567812345678".toByteArray()
            )
            .build()
        val ed41 = encrypter4.encrypt(originalData)
        val ed42 = encrypter4.encrypt(originalData)
        val ed43 = encrypter4.encrypt(originalData, false)
        val ed44 = encrypter4.encrypt(originalData, false)
        assertEquals(ed41, ed42)
        assertEquals(ed42, ed43)
        assertEquals(ed43, ed44)
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
    fun testGetOrNullFunctions() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val e1 = encrypter.encrypt(source.toByteArray(), false)
            val e11 = encrypter.encryptOrNull(source.toByteArray(), false)
            assertEquals(e1.toStringData(), e11?.toStringData())

            val e2 = encrypter.encrypt(source, false)
            val e21 = encrypter.encryptOrNull(source, false)
            assertEquals(e2, e21)

            assertTrue(
                encrypter.decrypt(e1).contentEquals(
                    source.toByteArray()
                )
            )
            assertTrue(
                encrypter.decryptOrNull(e1).contentEquals(
                    source.toByteArray()
                )
            )

            assertTrue(
                encrypter.decrypt(e1.toByteArray()).contentEquals(
                    source.toByteArray()
                )
            )
            assertTrue(
                encrypter.decryptOrNull(e1.toByteArray()).contentEquals(
                    source.toByteArray()
                )
            )

            assertTrue(
                encrypter.decrypt(e11!!.toByteArray()).contentEquals(
                    source.toByteArray()
                )
            )
            assertTrue(
                encrypter.decryptOrNull(e11.toByteArray()).contentEquals(
                    source.toByteArray()
                )
            )

            assertTrue(encrypter.decrypt(e2) == source)
            assertTrue(encrypter.decryptOrNull(e2) == source)

            assertTrue(encrypter.decrypt(e21!!) == source)
            assertTrue(encrypter.decryptOrNull(e21) == source)
        }
    }

    @Test
    fun testFileEncryption() {
        val originalContent = "Hello World".toByteArray()
        var originalFile: File? = null
        var encryptedFile: File? = null
        var decryptedFile: File? = null
        var encrypter: Encrypter? = null
        try {
            encrypter = Encrypter(appContext, "test_file_encryption")
            originalFile = File(appContext.filesDir, "original.txt")
            encryptedFile = File(appContext.filesDir, "encryptedFile.txt")
            decryptedFile = File(appContext.filesDir, "decryptedFile.txt")
            originalFile.writeBytes(originalContent)
            assertTrue(originalFile.readBytes().contentEquals(originalContent))

            assertTrue(encrypter.encryptFile(originalFile.absolutePath, encryptedFile.absolutePath))

            assertFalse(
                encryptedFile.readBytes().contentEquals(originalContent)
            )
            assertFalse(
                encryptedFile.readBytes().contentEquals(originalFile.readBytes())
            )
            assertTrue(
                encrypter.decryptFile(encryptedFile.absolutePath, decryptedFile.absolutePath)
            )
            assertTrue(
                decryptedFile.readBytes().contentEquals(originalContent)
            )
            assertTrue(
                decryptedFile.readBytes().contentEquals(originalFile.readBytes())
            )

        } finally {
            originalFile?.delete()
            encryptedFile?.delete()
            decryptedFile?.delete()
            encrypter?.resetKeys()
        }
    }

    @Test
    fun testResetKey() {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted = encrypter.encrypt(source)
            val decrypted = encrypter.decrypt(encrypted)

            assertEquals(source, decrypted)

            if (encrypter.resetKeys()) {
                val decrypted2 = encrypter.decryptOrNull(encrypted)
                assertNull(decrypted2)
            }
        }
    }
}