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
internal fun DecryptScreen(state: DecryptScreenState) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val decryptedText by state.decryptedText.collectAsStateWithLifecycle()
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
            singleLine = false,
            label = { Text("Input") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = decryptedText,
            onValueChange = {},
            label = { Text("Decrypted string") },
            singleLine = false,
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

    }

}

data class DecryptScreenState(
    val decryptedText: StateFlow<String>,
    val inputChanges: (String) -> Unit
)