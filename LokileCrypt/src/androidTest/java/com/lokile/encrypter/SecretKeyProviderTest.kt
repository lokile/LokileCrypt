package com.lokile.encrypter

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
internal class SecretKeyProviderTest {
    var provider: ISecretKeyProvider? = null
    lateinit var appContext: Context

    @Before
    fun before() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    fun tearDown() {
        provider?.clearKey()
    }

    @Test
    fun testAesKeyProvider() {
        provider = AESSecretKeyProvider("testAesKeyProvider")
        assertNotNull(provider?.getOrCreateKey())
    }
}