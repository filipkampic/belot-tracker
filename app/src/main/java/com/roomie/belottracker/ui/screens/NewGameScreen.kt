package com.roomie.belottracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.data.entities.Player
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
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }

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

            Text("Igrači (${state.selectedPlayerIds.size}/${viewModel.requiredPlayerCount()})", style = MaterialTheme.typography.titleMedium)
            PlayerPicker(
                players = state.allPlayers,
                selectedIds = state.selectedPlayerIds,
                onToggle = viewModel::togglePlayerSelection,
                onAddNewClick = { showAddPlayerDialog = true }
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

        if (showAddPlayerDialog) {
            AlertDialog(
                onDismissRequest = { showAddPlayerDialog = false },
                title = { Text("Novi igrač") },
                text = {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = { Text("Ime") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addNewPlayer(newPlayerName) {
                            newPlayerName = ""
                            showAddPlayerDialog = false
                        }
                    }) { Text("Dodaj") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPlayerDialog = false}) {
                        Text("Odustani")
                    }
                }
            )
        }
    }
}

@Composable
private fun PlayerPicker(players: List<Player>, selectedIds: List<Long>, onToggle: (Long) -> Unit, onAddNewClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        players.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { player ->
                    FilterChip(
                        selected = selectedIds.contains(player.id),
                        onClick = { onToggle(player.id) },
                        label = { Text(player.name) }
                    )
                }
            }
        }
        AssistChip(onClick = onAddNewClick, label = { Text("+ Novi igrač") })
    }
}
