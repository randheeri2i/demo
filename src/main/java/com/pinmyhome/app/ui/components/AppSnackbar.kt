package com.pinmyhome.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SnackbarState {
    val hostState = SnackbarHostState()
    var pendingMessage by mutableStateOf<String?>(null)
}

@Composable
fun rememberSnackbarState(): SnackbarState = remember { SnackbarState() }

fun showError(state: SnackbarState, message: String) {
    state.pendingMessage = message
}

@Composable
fun AppSnackbar(state: SnackbarState) {
    val pending = state.pendingMessage
    LaunchedEffect(pending) {
        if (!pending.isNullOrBlank()) {
            state.hostState.showSnackbar(pending)
            state.pendingMessage = null
        }
    }
    SnackbarHost(hostState = state.hostState) { data ->
        Snackbar(
            shape = RoundedCornerShape(12.dp),
            containerColor = Color(0xFF1B2A4A),
            contentColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(data.visuals.message, fontSize = 13.sp, color = Color.White)
        }
    }
}
