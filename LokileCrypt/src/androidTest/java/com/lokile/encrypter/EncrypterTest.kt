package com.lokile.encrypter

import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lokile.encrypter.encrypterImpl.asByteArray
import com.lokile.encrypter.encrypterImpl.asString
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.crypto.KeyGenerator


@RunWith(AndroidJUnit4::class)
class EncrypterTest {
    var encrypters = mutableListOf<Encrypter>()
    lateinit var appContext: Context

    @Before
    fun before() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            encrypters.add(Encrypter("p1"))
        }
        //test custom key
        encrypters.add(
            Encrypter(Encrypter.newSecretKey())
        )
        encrypters.add(
            Encrypter(
                KeyGenerator.getInstance("AES")
                    .apply {
                        init(256)
                    }.generateKey()
            )
        )
    }

    @Test
    fun encryptersInStringWithFixedIv() = runBlocking<Unit> {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted1 = encrypter.encryptOrNull(source)!!
            val decrypted1 = encrypter.decryptOrNull(encrypted1)
            assertEquals(decrypted1, source)
        }
    }

    @Test
    fun encryptersInStringWithRandomIv() = runBlocking<Unit> {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val encrypted1 = encrypter.encryptOrNull(source)!!
            val decrypted1 = encrypter.decryptOrNull(encrypted1)
            assertEquals(decrypted1, source)
            assertNotEquals(encrypter.encryptOrNull(source), encrypted1)
        }
    }

    @Test
    fun encryptersInByte() = runBlocking<Unit> {
        encrypters.forEach {
            val source = "DemoText"
            val encrypted1 = it.encryptOrNull(source.toByteArray())!!
            assertNotNull(encrypted1)
            val decrypted1 = it.decryptOrNull(encrypted1)!!
            val decrypted2 = it.decryptOrNull(encrypted1.asByteArray)!!
            assertTrue(decrypted1.contentEquals(decrypted2))
            assertEquals(source, String(decrypted1))
            assertEquals(source, String(decrypted2))
        }
    }

    @Test
    fun saveAndRemoveAesKey() = runBlocking<Unit> {
        val newKey = Encrypter.newSecretKey()
        Encrypter.saveSecretKeyToDevice(newKey, "key1")
        assertTrue(Encrypter.hasSecretKey("key1"))
        Encrypter.removeSecretKeyFromDevice("key1")
        assertFalse(Encrypter.hasSecretKey("key1"))
    }

    @Test
    fun encryptersInByteWithRandomIv() = runBlocking<Unit> {
        encrypters.forEach {
            val source = "DemoText"
            val encrypted1 = it.encryptOrNull(source.toByteArray())!!
            assertNotNull(encrypted1)
            val decrypted1 = it.decryptOrNull(encrypted1)!!
            val decrypted2 = it.decryptOrNull(encrypted1.asByteArray)!!

            assertTrue(decrypted1.contentEquals(decrypted2))
            assertEquals(source, String(decrypted1))
            assertEquals(source, String(decrypted2))

            assertNotEquals(
                it.encryptOrNull(source.toByteArray())!!.asString,
                encrypted1.asString
            )
        }
    }

    @Test
    fun fixed_IV_WithCustomKeyProvider() = runBlocking<Unit> {
        val originalData = " Hello world!"
        val encrypter1 = Encrypter(
            KeyGenerator.getInstance("AES")
                .apply {
                    init(256)
                }.generateKey(),
            "1234567812345678".toByteArray()
        )
        val ed11 = encrypter1.encryptOrNull(originalData)
        val ed12 = encrypter1.encryptOrNull(originalData)
        val ed13 = encrypter1.encryptOrNull(originalData)
        assertEquals(ed11, ed12)
        assertEquals(ed12, ed13)
        assertEquals(encrypter1.encryptOrNull(originalData), ed11)

        val encrypter2 = Encrypter(
            KeyGenerator.getInstance("AES")
                .apply {
                    init(256)
                }.generateKey()
        )
        val ed21 = encrypter2.encryptOrNull(originalData)
        val ed22 = encrypter2.encryptOrNull(originalData)
        val ed23 = encrypter2.encryptOrNull(originalData)
        val ed24 = encrypter2.encryptOrNull(originalData)
        assertNotEquals(ed21, ed22)
        assertNotEquals(ed22, ed23)
        assertNotEquals(ed23, ed24)

        val encrypter3 = Encrypter(
            KeyGenerator.getInstance("AES")
                .apply {
                    init(256)
                }.generateKey().encoded
        )
        val ed31 = encrypter3.encryptOrNull(originalData)
        val ed32 = encrypter3.encryptOrNull(originalData)
        val ed33 = encrypter3.encryptOrNull(originalData)
        val ed34 = encrypter3.encryptOrNull(originalData)
        assertNotEquals(ed31, ed32)
        assertNotEquals(ed33, ed34)

        val encrypter4 = Encrypter(
            KeyGenerator.getInstance("AES")
                .apply {
                    init(256)
                }.generateKey().encoded,
            "1234567812345678".toByteArray()
        )
        val ed41 = encrypter4.encryptOrNull(originalData)
        val ed42 = encrypter4.encryptOrNull(originalData)
        val ed43 = encrypter4.encryptOrNull(originalData)
        val ed44 = encrypter4.encryptOrNull(originalData)
        assertEquals(ed41, ed42)
        assertEquals(ed42, ed43)
        assertEquals(ed43, ed44)
    }

    @Test
    fun getOrNullFunctions() = runBlocking<Unit> {
        encrypters.forEach { encrypter ->
            val source = "DemoText"
            val e1 = encrypter.encryptOrNull(source.toByteArray())!!
            val e11 = encrypter.encryptOrNull(source.toByteArray())!!
            assertNotEquals(e1.asString, e11.asString)

            val e2 = encrypter.encryptOrNull(source)!!
            val e21 = encrypter.encryptOrNull(source)!!
            assertNotEquals(e2, e21)

            assertTrue(
                encrypter.decryptOrNull(e1).contentEquals(
                    source.toByteArray()
                )
            )
            assertTrue(
                encrypter.decryptOrNull(e2)!! == source
            )

            assertTrue(
                encrypter.decryptOrNull(e1.asByteArray).contentEquals(
                    source.toByteArray()
                )
            )

            assertTrue(
                encrypter.decryptOrNull(e11.asByteArray).contentEquals(
                    source.toByteArray()
                )
            )
            assertTrue(encrypter.decryptOrNull(e2) == source)
            assertTrue(encrypter.decryptOrNull(e21) == source)
        }
    }

    @Test
    fun largeFileEncryption() = runBlocking<Unit> {
        val originalContent = (0..1024 * 1024).joinToString(" ") { "Hello world $it" }.toByteArray()
        var originalFile: File? = null
        var encryptedFile: File? = null
        var decryptedFile: File? = null
        var encrypter: Encrypter?
        try {
            encrypter = Encrypter("test_file_encryption")
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
                decryptedFile.readBytes().contentEquals(originalFile.readBytes())
            )

            assertTrue(
                decryptedFile.readBytes().contentEquals(originalContent)
            )


        } finally {
            originalFile?.delete()
            encryptedFile?.delete()
            decryptedFile?.delete()
        }
    }
}