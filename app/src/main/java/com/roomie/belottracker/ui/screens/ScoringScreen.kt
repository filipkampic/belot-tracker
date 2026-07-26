package com.roomie.belottracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.data.entities.GameStatus
import com.roomie.belottracker.data.entities.TrumpSuit
import com.roomie.belottracker.ui.components.PrimaryButton
import com.roomie.belottracker.ui.components.RoundRow
import com.roomie.belottracker.ui.components.ScoreInputField
import com.roomie.belottracker.ui.components.SuitPicker
import com.roomie.belottracker.ui.components.TotalsCard
import com.roomie.belottracker.ui.components.ZvanjeChipRow
import com.roomie.belottracker.ui.viewmodel.ParticipantScoreInput
import com.roomie.belottracker.ui.viewmodel.ScoringViewModel
import com.roomie.belottracker.util.modeLabel
import kotlin.collections.find

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    gameId: Long,
    onBack: () -> Unit,
    onWinner: (Long, String) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToWinner.collect { winnerIndex -> onWinner(gameId, winnerIndex) }
    }

    val game = state.game
    val isFinished = game?.status == GameStatus.FINISHED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game?.let { modeLabel(it.mode) } ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TotalsCard(
                    participantNames = state.participantNames,
                    totals = state.totals,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(state.rounds) { round ->
                RoundRow(
                    roundNumber = round.roundNumber,
                    trump = round.trump,
                    trumpPickerIndex = round.trumpPickerIndex,
                    participantNames = state.participantNames,
                    scores = state.scoresByRound[round.id] ?: emptyList(),
                    onClick = { viewModel.startEditRound(round) }
                )
                HorizontalDivider()
            }

            if (!isFinished) {
                item {
                    RoundInputPanel(
                        participantNames = state.participantNames,
                        inputs = state.roundInputs,
                        selectedTrump = state.selectedTrump,
                        trumpPickerIndex = state.trumpPickerIndex,
                        onPickerSelected = { viewModel.setTrumpPickerIndex(it) },
                        useZvanjeBela = game?.useZvanjeBela ?: false,
                        canSubmit = viewModel.canSubmitRound(),
                        onTrumpSelected = { suit -> viewModel.selectTrump(suit) },
                        onBaseChange = viewModel::updateBasePoints,
                        onZvanjeToggle = viewModel::toggleZvanje,
                        onBelaToggle = viewModel::toggleBela,
                        onSubmit = viewModel::submitRound
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundInputPanel(
    participantNames: List<String>,
    inputs: List<ParticipantScoreInput>,
    selectedTrump: TrumpSuit?,
    trumpPickerIndex: Int?,
    onPickerSelected: (Int) -> Unit,
    useZvanjeBela: Boolean,
    canSubmit: Boolean,
    onTrumpSelected: (TrumpSuit) -> Unit,
    onBaseChange: (Int, String) -> Unit,
    onZvanjeToggle: (Int, String) -> Unit,
    onBelaToggle: (Int) -> Unit,
    onSubmit: () -> Unit,
    submitLabel: String = "Dodaj krug"
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        var expanded by remember { mutableStateOf(false) }
        Text("Tko bira adut?", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = trumpPickerIndex?.let { participantNames[it] } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Igrač") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                participantNames.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onPickerSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }

        Text("Adut", style = MaterialTheme.typography.labelLarge)
        SuitPicker(selectedSuit = selectedTrump, onSuitSelected = onTrumpSelected)

        participantNames.forEachIndexed { index, name ->
            val input = inputs.find { it.participantIndex == index }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, style = MaterialTheme.typography.labelLarge)
                ScoreInputField(
                    label = "Bodovi",
                    value = input?.basePoints ?: "",
                    onValueChange = { onBaseChange(index, it) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (useZvanjeBela) {
                    ZvanjeChipRow(
                        selectedEvents = input?.zvanjeEvents ?: emptyList(),
                        onToggle = { value -> onZvanjeToggle(index, value) },
                        belaSelected = input?.bela ?: false,
                        onBelaToggle = { onBelaToggle(index) }
                    )
                }
            }
        }

        PrimaryButton(
            text = submitLabel,
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
