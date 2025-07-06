# LokileCrypt-Android

https://medium.com/system-weakness/simple-encryption-library-in-android-app-b28218f6a14d

## Overview
This library performs encryption and decryption using the AES 256-bit encryption algorithm. It uses [Android KeyStore System](https://developer.android.com/training/articles/keystore.html) to make it more difficult to extract the secret key from the device. In addition to using the KeyStore, the library also allows you to provide your own secret key, enabling you to encrypt or decrypt data that is transferred outside of the app

## Requirements
- Android API 18 or higher
- Java 1.8+

## Installation
Add the following code to your root build.gradle at the end of repositories:
```
  allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
  }
```
Then, add the dependency to your app build.gradle file, the latest version is: [![](https://jitpack.io/v/lokile/LokileCrypt.svg)](https://jitpack.io/#lokile/LokileCrypt)
```
  dependencies {
    implementation 'com.github.lokile:LokileCrypt:latest_version'
  }
```

## Usage:
### Create the object:
- Just create it:
```
import com.lokile.encrypter.encrypters.Encrypter
......
var encrypter = Encrypter(alias="your_alias")
```
- Or you can use the Builder for more options:
```
import com.lokile.encrypter.encrypters.Encrypter
......
var encrypter = Encrypter
      .Builder("your_alias")
      
      //set your own keys instead of using Android KeyStore System
      .setSecretKey(your_aes_key, your_iv_key) 

      //default is "AES/CBC/PKCS7PADDING"
      .setEncryptAlgorithm("your_encrypt_algorithm") 

      .build()
```

The library requires an alias that is bound to a SecretKey in the KeyStore. Using this alias, the library can retrieve the key from the KeyStore. Typically, this alias is associated with a user, which is especially useful if your application supports multiple account logins

### Encrypt your data:
- Just call the function `encryptOrNull` to perform encryption. It will return `null` if there is an issue.
```
val toBeEncrypted="Hello World!"
val result1:String? = encrypter.encryptOrNull(toBeEncrypted)
val result2:String? = encrypter.encryptOrNull(toBeEncrypted) 
// result1 != result2
```
In the function above, the library generates a new random IV by default when performing encryption, so the encrypted result will differ even for the same input
- The above functions merges the IV key and the encrypted data into a single output String, If you want to separate them, you can update the code as the following:
```
val result1:EncryptedData? = encrypter.encryptOrNull(toBeEncrypted.toByteArray())
//result1.data
//result1.iv
//result1.stringData
//result1.toByteArray()
```

### Here is how to decrypt your data:
You can use the `decryptOrNull` function to perform decryption, and it will return `null` if there is an issue:

```
val decrypted1:ByteArray? = encrypter.decryptOrNull(encrypted1)
// val decrypted1Str = String(decrypted1)
```

### Save your AesKey:
– This library supports saving your AES key into the Android KeyStore, so you no longer need to manage storing the SecretKey in a secure location yourself:
```
Encrypter.saveAesKeyToKeyStore(yourKeyInByteArray, yourNewAlias)
//next: use the `yourNewAlias` to create the new `Encrypter` object
```
### Generate a new random AesKey:
```
val newKey:ByteArray = Encrypter.getRandomAesKey(keySize)
```

### This library also supports to work with large files
#### Encrypt a file:
```
val result:Boolean = encrypter.encryptFile(originalPath, encryptedFilePath)
```
#### Decrypt a file:
```
val result:Boolean = encrypter.decryptFile(encryptedFilePath, decryptedFilePath)
```

## Want to contribute? ##

Fell free to contribute, I really like pull requests :octocat:
