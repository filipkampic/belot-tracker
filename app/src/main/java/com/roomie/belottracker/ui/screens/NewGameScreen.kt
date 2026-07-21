package com.roomie.belottracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.GameMode.TWO_V_TWO
import com.roomie.belottracker.ui.components.ModeSegmentedControl
import com.roomie.belottracker.ui.components.PrimaryButton
import com.roomie.belottracker.ui.viewmodel.NewGameViewModel
import com.roomie.belottracker.util.targetScoreOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameScreen(
    onGameCreated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: NewGameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova igra") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Način igre", style = MaterialTheme.typography.titleMedium)
            ModeSegmentedControl(
                selectedMode = state.mode,
                onModeSelected = viewModel::selectMode,
                modifier = Modifier
            )

            Text(
                "Igrači (${state.players.count { it.isNotBlank() }}/${viewModel.requiredPlayerCount()})",
                style = MaterialTheme.typography.titleMedium
            )
            PlayerPicker(
                players = state.players,
                requiredCount = viewModel.requiredPlayerCount(),
                mode = state.mode,
                onEditPlayer = { index, newName ->
                    viewModel.editPlayer(index, newName)
                }
            )

            Text("Cilj bodova", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                targetScoreOptions(state.mode).forEach { score ->
                    FilterChip(
                        selected = state.targetScore == score,
                        onClick = { viewModel.selectTargetScore(score) },
                        label = { Text(score.toString()) }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zvanja i bela", style = MaterialTheme.typography.titleMedium)
                Switch(checked = state.useZvanjeBela, onCheckedChange = viewModel::toggleZvanjeBela)
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = "Započni igru",
                onClick = { viewModel.createGame(onGameCreated) },
                enabled = viewModel.canStartGame(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlayerPicker(
    players: List<String>,
    requiredCount: Int,
    mode: GameMode,
    onEditPlayer: (Int, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (mode == TWO_V_TWO) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    PlayerInputField(
                        label = "Tim 1 - Igrač 1",
                        value = players.getOrNull(0) ?: "",
                        onValueChange = { onEditPlayer(0, it) }
                    )
                    PlayerInputField(
                        label = "Tim 1 - Igrač 2",
                        value = players.getOrNull(1) ?: "",
                        onValueChange = { onEditPlayer(1, it) }
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    PlayerInputField(
                        label = "Tim 2 - Igrač 1",
                        value = players.getOrNull(2) ?: "",
                        onValueChange = { onEditPlayer(2, it) }
                    )
                    PlayerInputField(
                        label = "Tim 2 - Igrač 2",
                        value = players.getOrNull(3) ?: "",
                        onValueChange = { onEditPlayer(3, it) }
                    )
                }
            }
        } else {
            repeat(requiredCount) { index ->
                PlayerInputField(
                    label = "Igrač ${index + 1}",
                    value = players.getOrNull(index) ?: "",
                    onValueChange = { onEditPlayer(index, it) }
                )
            }
        }
    }
}

@Composable
private fun PlayerInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it.trim()) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        singleLine = true,
        label = { Text(label) }
    )
}
