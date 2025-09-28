package com.lokile.dataencrypterdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokile.dataencrypterdemo.ui.DecryptScreenState
import com.lokile.dataencrypterdemo.ui.EncryptScreenState
import com.lokile.encrypter.Encrypter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val decryptedText = MutableStateFlow("")
    private val encryptedText = MutableStateFlow("")
    private val encrypter = Encrypter("user_id_1")

    val encryptState = EncryptScreenState(
        encryptedText = encryptedText,
        inputChanges = {
            viewModelScope.launch {
                encryptedText.value = encrypter.encryptOrNull(it).orEmpty()
            }
        }
    )

    val decryptState = DecryptScreenState(
        decryptedText = decryptedText,
        inputChanges = {
            viewModelScope.launch {
                decryptedText.value = encrypter.decryptOrNull(it).orEmpty()
            }
        }
    )
}