package com.lokile.dataencrypterdemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun EncryptScreen(state: EncryptScreenState) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val encryptedText by state.encryptedText.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        TextField(
            value = inputText,
            onValueChange = {
                inputText = it
                state.inputChanges(it)
            },
            label = { Text("Input") },
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = encryptedText,
            onValueChange = {},
            singleLine = false,
            readOnly = true,
            label = { Text("Encrypted string") },
            modifier = Modifier.fillMaxWidth()
        )
    }

}

data class EncryptScreenState(
    val encryptedText: StateFlow<String>,
    val inputChanges: (String) -> Unit,
)