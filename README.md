# LokileCrypt-Android



## Overview
This library performs encryption and decryption using the AES 256-bit encryption algorithm. It uses [Android KeyStore System](https://developer.android.com/training/articles/keystore.html) to makes it more difficult to extract the secret key from the device. 
The library also allows you to provide your secret key to encrypt/decrypt so that you can transfer/received the encrypted data to/from outside of the app.

## Requirements
- Android API 18 or higher
- Android Studio 4.1+
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
- The simple way:
```
import com.lokile.encrypter.encrypters.imp.Encrypter
......
var encrypter = Encrypter(context=context,alias="your_alias")
//alias: your keyword to communicate with Android KeyStore System
```
- Or you can use the builder:
```
import com.lokile.encrypter.encrypters.imp.Encrypter
......
var encrypter = Encrypter
                  .Builder(appContext, "your_alias")
                  .setSecretKey(your_aes_key, your_iv_key) //set your own keys instead of using Android KeyStore System
                  .setEncryptAlgorithm("your_encrypt_algorithm") //default is "AES/CBC/PKCS7PADDING"
                  .setErrorListener { encrypterError, throwable ->  }
                  .build()
```
### Encrypt your data:
- The simple was to perform encryption (the IV key is randomized):
```
val toBeEncrypted="Hello World!"
val result1:String? = encrypter.encrypt(toBeEncrypted)
val result2:String? = encrypter.encrypt(toBeEncrypted) // result1 != result2

/* The library will generate a new randomized IV key when performing encryption,
so the encrypted results are not the same for the same input*/
```
- To make the encrypted data are the same for the same input (NOT randomized IV key):
```
val toBeEncrypted="Hello World!"
val result1:String? = encrypter.encrypt(toBeEncrypted, useRandomizeIv=false)
val result2:String? = encrypter.encrypt(toBeEncrypted, useRandomizeIv=false) // result1 == result2
```
- The above functions merges the IV key and the encrypted data into single output String, use the following code instead to separate these data:
```
val result1:EncryptedData? = encrypter.encrypt(toBeEncrypted.toByteArray())
//result1.data
//result1.iv
//result1.toStringData() //to merge the data and iv to a single String
//result1.toByteArray()  //to merge the data and iv to a single ByteArray
```

### Decrypt your data:

```
val decrypted1:ByteArray? = encrypter.decrypt(encrypted1)
// val decrypted1Str = String(decrypted1)
```

## License:
MIT is the project license so feel free to use it :tada:

## Want to contribute? ##

Fell free to contribute, I really like pull requests :octocat:
