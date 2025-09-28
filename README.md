# LokileCrypt-Android

[Simple Encryption Library in Android App](https://medium.com/system-weakness/simple-encryption-library-in-android-app-b28218f6a14d)

## Overview
LokileCrypt is a lightweight library for AES 256-bit encryption and decryption.  
It integrates with the [Android KeyStore System](https://developer.android.com/training/articles/keystore.html), making it harder to extract keys from a device.  
In addition, you can provide your own AES key, allowing you to encrypt or decrypt data that needs to be shared outside the app.

## Features
- **AES-256 encryption/decryption** for strings, byte arrays, and files  
- **Android KeyStore integration** (store and retrieve keys by ID)  
- **Custom key support** (use your own `Key` or `ByteArray`)  
- **Automatic IV handling** (unique IV per encryption)  
- **Key management utilities**: save, remove, and check if a key exists  
- **Large file support**: encrypt and decrypt files efficiently  
- **Coroutines support** with `suspend` functions  

## Requirements
- Android API 23 or higher

## Installation
Add the dependency to your app’s `build.gradle` file. The latest version is:  
![Maven Central](https://img.shields.io/maven-central/v/io.github.lokile/lokile-crypt?label=Maven%20Central)

```gradle
dependencies {
    implementation("io.github.lokile:lokile-crypt:latest_version")
}
```

## Usage:
### 1. Create the object:
You can create an `Encrypter` in three ways:
```
// 1. Using an ID (stored in Android KeyStore)
val encrypter1 = Encrypter(id = "user_123")

// 2. Using an existing AES Key object
val encrypter2 = Encrypter(aesKey = key, iv = ivBytes)

// 3. Using a raw AES key as ByteArray
val encrypter3 = Encrypter(aesKey = rawKeyBytes, iv = ivBytes)

```
### 2. Encrypt data
```
val text = "Hello World!"
val encrypted: String? = encrypter1.encryptOrNull(text)
```
- Encrypt as `EncryptedData` (access raw bytes and IV separately):
```
val encrypted: EncryptedData? = encrypter1.encryptOrNull(text.toByteArray())
// encrypted.data
// encrypted.iv
// encrypted.stringData
// encrypted.toByteArray()

```

### 3. Decrypt data
```
val decrypted: String? = encrypter1.decryptOrNull(encryptedString)

val decryptedBytes: ByteArray? = encrypter1.decryptOrNull(encryptedData)
val decryptedStr = decryptedBytes?.let { String(it) }

```

### 4. Work with files
```
val successEncrypt: Boolean = encrypter1.encryptFile("path/to/input.txt", "path/to/encrypted.bin")
val successDecrypt: Boolean = encrypter1.decryptFile("path/to/encrypted.bin", "path/to/decrypted.txt")
```

### 5. Key management
```
// Save your own AES key into Android KeyStore
Encrypter.saveSecretKeyToDevice(aesKeyBytes, "user_123")

// Remove a stored key
Encrypter.removeSecretKeyFromDevice("user_123")

// Check if a key exists
val hasKey: Boolean = Encrypter.hasSecretKey("user_123")

// Generate a new random AES key
val newKey: ByteArray = Encrypter.newSecretKey() // default = 256 bits

```

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests :octocat:
