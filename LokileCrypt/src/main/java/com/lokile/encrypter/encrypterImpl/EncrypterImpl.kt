package com.lokile.encrypter.encrypterImpl

import com.lokile.encrypter.Encrypter
import com.lokile.encrypter.secretKeyProviders.ISecretKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

internal class EncrypterImpl(
    keyProvider: ISecretKeyProvider,
    algorithm: String?
) : Encrypter {

    private val cipherProvider = CipherProvider(
        keyProvider = keyProvider,
        algorithm = algorithm ?: "AES/CBC/PKCS7PADDING"
    )

    override suspend fun encryptOrNull(data: ByteArray): EncryptedData? = encryptDataInternal(data)

    override suspend fun encryptOrNull(data: String): String? =
        encryptDataInternal(data.toByteArray())?.asString

    override suspend fun encryptFile(
        inputPath: String,
        encryptedFilePath: String
    ): Boolean = encryptFileInternal(inputPath, encryptedFilePath)

    override suspend fun decryptFile(
        encryptedFilePath: String,
        outputFilePath: String
    ): Boolean = decryptFileInternal(encryptedFilePath, outputFilePath)


    override suspend fun decryptOrNull(data: ByteArray, iv: ByteArray?): ByteArray? {
        return try {
            if (iv != null) {
                decryptDataInternal(EncryptedData(data, iv))
            } else {
                decryptDataInternal(data.asEncryptedData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override suspend fun decryptOrNull(data: String): String? {
        return decryptDataInternal(data.asEncryptedData)?.let { String(it) }
    }

    override suspend fun decryptOrNull(data: EncryptedData): ByteArray? {
        return decryptDataInternal(data)
    }

    private suspend fun encryptFileInternal(
        inputPath: String,
        encryptedOutputFile: String,
    ): Boolean {
        val inputFile = File(inputPath)
        val outputFile = File(encryptedOutputFile)
        return withContext(Dispatchers.IO) {
            if (inputFile.absolutePath == outputFile.absolutePath) {
                return@withContext false
            }
            if (!inputFile.exists()) {
                return@withContext false
            }
            try {
                inputFile.inputStream().use { input ->
                    outputFile.outputStream().use { output ->
                        val cipher = cipherProvider.getEncryptCipher()

                        // Store the IV first
                        output.write(
                            ByteBuffer.allocate(cipher.iv.size + 1)
                                .apply {
                                    put(cipher.iv.size.toByte())
                                    put(cipher.iv)
                                }.array()
                        )

                        flow {
                            // Read the input stream in 8 KB chunks and emit each as Pair<ByteArray, Boolean>.
                            // Boolean indicates if more data may remain (based on input.available()).

                            val chunkContainer = ByteArray(1024 * 8)
                            var chunkSize = input.read(chunkContainer)
                            while (chunkSize >= 0) {
                                val chunk = if (chunkSize == chunkContainer.size) {
                                    chunkContainer
                                } else {
                                    chunkContainer.copyOfRange(0, chunkSize)
                                }
                                emit(chunk to (input.available() > 0))
                                chunkSize = input.read(chunkContainer)
                            }
                        }.map { (chunk, available) ->
                            // Encrypting
                            if (available) {
                                cipher.update(chunk)
                            } else {
                                cipher.doFinal(chunk)
                            }
                        }.onEach {
                            // Write encrypted data to file
                            output.write(
                                ByteBuffer.allocate(Int.SIZE_BYTES + it.size)
                                    .apply {
                                        put(it.size.asBytes) // data size
                                        put(it) // encrypted data
                                    }.array()
                            )
                        }.collect()

                        return@withContext true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                outputFile.delete()
                return@withContext false
            }
        }
    }

    private suspend fun decryptFileInternal(
        encryptedFilePath: String,
        outputFilePath: String
    ): Boolean {
        val inputFile = File(encryptedFilePath)

        if (!inputFile.exists()) {
            return false
        }

        val outputFile = File(outputFilePath)
        return withContext(Dispatchers.IO) {
            try {
                inputFile.inputStream().use { input ->
                    outputFile.outputStream().use { output ->
                        var buffer = ByteArray(1024 * 8)
                        var loadedBytes: Int

                        // Load IV
                        input.read(buffer, 0, 1)
                        val ivSize = buffer[0].toInt()
                        input.read(buffer, 0, ivSize)

                        val cipher =
                            cipherProvider.getDecryptCipher(iv = buffer.copyOfRange(0, ivSize))

                        flow {
                            // Loading input file
                            while (true) {
                                loadedBytes = input.read(buffer, 0, Int.SIZE_BYTES)

                                // End of file
                                if (loadedBytes <= 0) break

                                val chunkSize = buffer.asInt
                                if (buffer.size != chunkSize) {
                                    buffer = ByteArray(chunkSize)
                                }

                                loadedBytes = input.read(buffer, 0, chunkSize)
                                if (loadedBytes != chunkSize) {
                                    throw RuntimeException("File is invalid")
                                }

                                emit(
                                    buffer to (input.available() > 0)
                                )
                            }
                        }
                            // Decrypting
                            .map { (chunk, available) ->
                                if (available) {
                                    cipher.update(chunk)
                                } else {
                                    cipher.doFinal(chunk)
                                }
                            }
                            // Write to output
                            .onEach {
                                output.write(it)
                            }
                            .collect()

                        true
                    }
                }
            } catch (e: Exception) {
                outputFile.delete()
                e.printStackTrace()

                false
            }
        }
    }

    private suspend fun encryptDataInternal(data: ByteArray): EncryptedData? {
        return withContext(Dispatchers.Default) {
            try {
                val cipher = cipherProvider.getEncryptCipher()
                val output = cipher.doFinal(data)
                val iv = cipher.iv
                EncryptedData(output, iv)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun decryptDataInternal(data: EncryptedData): ByteArray? {
        return withContext(Dispatchers.Default) {
            try {
                cipherProvider.getDecryptCipher(data.iv).doFinal(data.data)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}