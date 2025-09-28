package com.lokile.dataencrypterdemo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.lokile.compose.modifiers.AnimatedStyle
import io.lokile.compose.modifiers.animatedBorder
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun MainScreen(
    encryptState: EncryptScreenState,
    decryptState: DecryptScreenState
) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        val pagerState = rememberPagerState(pageCount = { 2 })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
        ) { page ->
            if (page == 0) {
                EncryptScreen(encryptState)
            } else {
                DecryptScreen(decryptState)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TabButton(
                text = "Encrypt",
                onClick = {
                    pagerState.animateScrollToPage(0)
                },
                active = pagerState.currentPage == 0,
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Decrypt",
                onClick = {
                    pagerState.animateScrollToPage(1)
                },
                active = pagerState.currentPage == 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    onClick: suspend () -> Unit,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                onClick = {
                    coroutineScope.launch {
                        onClick()
                    }
                },
                role = Role.Button,
            )
            .background(
                if (active) Color.Gray else Color.Transparent
            )
            .animatedBorder(
                enabled = !active,
                shape = CircleShape,
                duration = 2.seconds,
                style = AnimatedStyle.Gradient,
                colors = listOf(
                    Color.Blue.copy(alpha = 0.8f),
                    Color.Gray.copy(alpha = 0.5f),
                    Color.Gray.copy(alpha = 0.3f),
                    Color.Gray.copy(alpha = 0.1f),
                )
            )
            .padding(16.dp),

        )
    {
        Text(text = text)
    }
}