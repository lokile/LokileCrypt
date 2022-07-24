# LokileCrypt-Android



## Overview
This library performs encryption and decryption using the AES 256-bit encryption algorithm. It uses [Android KeyStore System](https://developer.android.com/training/articles/keystore.html) to make it more difficult to extract the secret key from the device. Besides using the KeyStore, the library also allows you to provide your secret key to encrypt/decrypt so that you can transfer or receive the encrypted data outside of the app.

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
import com.lokile.encrypter.encrypters.imp.Encrypter
......
var encrypter = Encrypter(context=context,alias="your_alias")
```
- Or you can use the Builder for more options:
```
import com.lokile.encrypter.encrypters.imp.Encrypter
......
var encrypter = Encrypter
      .Builder(appContext, "your_alias")
      
      //set your own keys instead of using Android KeyStore System
      .setSecretKey(your_aes_key, your_iv_key) 

      //default is "AES/CBC/PKCS7PADDING"
      .setEncryptAlgorithm("your_encrypt_algorithm") 

      .build()
```

The library needs the alias to bound to SecretKey in KeyStore. Using this alias the library will be able to retrieve it from KeyStore. Usually this alias bound to user, it is very helpful if your application support multi accounts login.

### Encrypt your data:
- Just call the function `encryptOrNull` to perform encryption. It will return `null` if there is an issue. Or you can handle the exception yourself by using the `encrypt` function
```
val toBeEncrypted="Hello World!"
val result1:String? = encrypter.encryptOrNull(toBeEncrypted)
val result2:String? = encrypter.encryptOrNull(toBeEncrypted) 
// result1 != result2
```
In the above function, the library will generate a new randomized IV key by default when performing encryption, so the encrypted results are not the same for the same input
- If you want to make the encrypted data are the same for the same input, you can set the `useRandomizeIv=false` as the following:
```
val toBeEncrypted="Hello World!"
val result1:String? = encrypter.encryptOrNull(toBeEncrypted, useRandomizeIv=false)
val result2:String? = encrypter.encryptOrNull(toBeEncrypted, useRandomizeIv=false) 
// result1 == result2
```
- The above functions merges the IV key and the encrypted data into a single output String, If you want to separate them, you can update the code as the following:
```
val result1:EncryptedData? = encrypter.encryptOrNull(toBeEncrypted.toByteArray())
//result1.data
//result1.iv
//result1.toStringData()
//result1.toByteArray()
```

### Save your AesKey:
- This library supports to save your AesKey into Android KeyStore, so that you don't need to manage to save you SecretKey anymore:
```
context.saveAesKeyToKeyStore(yourKeyInByteArray, yourNewAlias)
//next: use the `yourNewAlias` to create the new `Encrypter` object
```

### Finally, here is how to decrypt your data:
You can use the `decryptOrNull` function to perform decryption, and it will return `null` if there is an issue. Or you can use the `decrypt` function to handle the exception yourself:

```
val decrypted1:ByteArray? = encrypter.decryptOrNull(encrypted1)
// val decrypted1Str = String(decrypted1)
```


## Want to contribute? ##

Fell free to contribute, I really like pull requests :octocat:
