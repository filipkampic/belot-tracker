package com.roomie.belottracker.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.ui.components.PrimaryButton
import com.roomie.belottracker.ui.viewmodel.WinnerViewModel
import androidx.compose.animation.core.Animatable

@Composable
fun WinnerScreen(onSaveAndExit: () -> Unit, viewModel: WinnerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(state) {
        if (state != null) scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale.value)) {
                Text("🏆", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(16.dp))
                Text("Pobjednik!", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                state?.let {
                    Text(it.winnerName, style = MaterialTheme.typography.titleLarge)
                    Text("${it.finalScore} bodova", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(32.dp))
                PrimaryButton(text = "Spremi i izađi", onClick = onSaveAndExit)
            }
        }
    }
}
