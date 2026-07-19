package com.roomie.belottracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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

            Text("Igrači (${state.selectedPlayers.size}/${viewModel.requiredPlayerCount()})", style = MaterialTheme.typography.titleMedium)
            PlayerPicker(
                selectedPlayers = state.selectedPlayers,
                onEditPlayer = viewModel::editPlayer,
                onRemovePlayer = viewModel::removePlayerFromGame
            )
            Button(onClick = { showAddPlayerDialog = true }) {
                Text("+ Dodaj igrača")
            }

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
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = { newPlayerName = it },
                            label = { Text("Novi igrač") },
                            singleLine = true
                        )

                        var expanded by remember { mutableStateOf(false) }

                        Box {
                            Button(onClick = { expanded = true }) {
                                Text("Dodaj postojećeg")
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                state.allPlayers
                                    .filter { p -> state.selectedPlayers.none { it.id == p.id } }
                                    .forEach { player ->
                                        DropdownMenuItem(
                                            text = { Text(player.name) },
                                            onClick = {
                                                viewModel.addExistingPlayer(player)
                                                expanded = false
                                                newPlayerName = ""
                                                showAddPlayerDialog = false
                                            }
                                        )
                                    }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addNewPlayer(newPlayerName)
                        newPlayerName = ""
                        showAddPlayerDialog = false
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
private fun PlayerPicker(
    selectedPlayers: List<Player>,
    onEditPlayer: (Player) -> Unit,
    onRemovePlayer: (Player) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        selectedPlayers.forEach { player ->
            key(player.id) {
                var isEditing by remember(player.id) { mutableStateOf(false) }
                var editedName by remember(player.id) { mutableStateOf(player.name) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        readOnly = !isEditing
                    )
                    if (isEditing) {
                        IconButton(onClick = {
                            if (editedName.isNotBlank()) {
                                onEditPlayer(player.copy(name = editedName.trim()))
                            }
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Spremi")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Uredi")
                        }
                    }

                    IconButton(onClick = { onRemovePlayer(player) }) {
                        Icon(Icons.Default.Close, contentDescription = "Ukloni")
                    }
                }
            }
        }
    }
}
