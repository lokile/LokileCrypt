package com.lokile.encrypter.encrypters

import com.lokile.encrypter.EncryptedData
import com.lokile.encrypter.ISecretKeyProvider
import com.lokile.encrypter.toByteArray
import com.lokile.encrypter.toInt
import java.io.File
import java.nio.ByteBuffer

internal class BaseEncrypter(
    keyProvider: ISecretKeyProvider,
    algorithm: String?
) {
    private val cipherProvider = CipherProvider(
        keyProvider = keyProvider,
        algorithm = algorithm ?: "AES/CBC/PKCS7PADDING"
    )

    fun encryptFile(
        inputPath: String,
        encryptedOutputFile: String,
    ): Boolean {
        synchronized(this) {
            val inputFile = File(inputPath)
            val outputFile = File(encryptedOutputFile)
            if (inputFile.absolutePath == outputFile.absolutePath) {
                return false
            }
            if (!inputFile.exists()) {
                return false
            }
            try {
                inputFile.inputStream().use { input ->
                    outputFile.outputStream().use { output ->
                        val buffer = ByteArray(1024 * 8)
                        var bytes = input.read(buffer)
                        val cipher = cipherProvider.getEncryptCipher()
                        output.write(
                            ByteBuffer.allocate(cipher.iv.size + 1)
                                .apply {
                                    put(cipher.iv.size.toByte())
                                    put(cipher.iv)
                                }.array()
                        )
                        while (bytes >= 0) {
                            val inputBytes = if (bytes == buffer.size) {
                                buffer
                            } else {
                                buffer.copyOfRange(0, bytes)
                            }

                            val encryptedBytes = if (input.available() > 0) {
                                cipher.update(inputBytes)
                            } else {
                                cipher.doFinal(inputBytes)
                            }

                            output.write(
                                ByteBuffer.allocate(Int.SIZE_BYTES + encryptedBytes.size)
                                    .apply {
                                        put(encryptedBytes.size.toByteArray())
                                        put(encryptedBytes)
                                    }.array()
                            )
                            bytes = input.read(buffer)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }
    }

    fun decryptFile(encryptedFilePath: String, outputFilePath: String): Boolean {
        synchronized(this) {
            val inputFile = File(encryptedFilePath)
            val outputFile = File(outputFilePath)
            if (!inputFile.exists()) {
                return false
            }
            try {
                inputFile.inputStream().use { input ->
                    outputFile.outputStream().use { output ->
                        var buffer = ByteArray(1024 * 8)
                        var bytesLength: Int

                        input.read(buffer, 0, 1)
                        val ivSize = buffer[0].toInt()
                        input.read(buffer, 0, ivSize)

                        val cipher = cipherProvider.getDecryptCipher(buffer.copyOfRange(0, ivSize))

                        while (true) {
                            bytesLength = input.read(buffer, 0, Int.SIZE_BYTES)
                            if (bytesLength <= 0) {
                                return true
                            }

                            val dataSize = buffer.toInt()
                            if (buffer.size < dataSize) {
                                buffer = ByteArray(dataSize)
                            }
                            bytesLength = input.read(buffer, 0, dataSize)
                            if (bytesLength <= 0) {
                                return false
                            }

                            val originBytes = if (input.available() > 0) {
                                cipher.update(buffer.copyOfRange(0, dataSize))
                            } else {
                                cipher.doFinal(buffer.copyOfRange(0, dataSize))
                            }
                            output.write(originBytes)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }

    fun encryptData(data: ByteArray): EncryptedData {
        synchronized(this) {
            val cipher = cipherProvider.getEncryptCipher()
            val output = cipher.doFinal(data)
            val iv = cipher.iv
            return EncryptedData(output, iv)
        }
    }

    fun decryptData(data: EncryptedData): ByteArray {
        synchronized(this) {
            return cipherProvider.getDecryptCipher(data.iv).doFinal(data.data)
        }
    }
}