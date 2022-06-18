# LokileCrypt-Android



## Overview
This library provides a simple way to encrypt and decrypt data using the AES 256-bit encryption algorithm. The library store the secret key in the Android Keystore which makes it more difficult to extract from the device. This library also allows you to provide your secret key to encrypt/decrypt so that you can transfer the data with your server

## Requirements
- Android API 18 or higher
- Android Studio 4.1+
- Java 1.8+

## Installation
Add the following code to to your root build.gradle at the end of repositories:
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
The library is easy to use. Just create the Encrypter object and use it to encrypt/decrypt your data:
```
import com.lokile.dataencrypter.encrypters.imp.Encrypter
......
var encrypter: Encrypter
try {
  encrypter = Encrypter(
    context=context,
    alias="your_alias" // it's a keyword to save/load the secret key in keystore
  )
} catch(e: Exception) {
  Log.e(TAG, "Failed to create the Encrypter", e)
}
```
To encrypt the data:
(For more security, the library will generate a new randomized IV key when performing encrypt, 
so the encrypted results are not the same for the same input)
```
val toBeEncrypted="Hello World!"
val result1:String = encrypter.encrypt(toBeEncrypted)
val result2:String = encrypter.encrypt(toBeEncrypted) // result1 != result2
```
If we expected that the encrypted data should be the same for the same input. We can update the code as the following:
```
val toBeEncrypted="Hello World!"
val result1:String = encrypter.encrypt(toBeEncrypted, useRandomizeIv=false)
val result2:String = encrypter.encrypt(toBeEncrypted, useRandomizeIv=false) // result1 == result2
```

