package com.roomie.belottracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TotalsCard(
                participantNames = state.participantNames,
                totals = state.totals,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                itemsIndexed(state.rounds) { _, round ->
                    RoundRow(
                        roundNumber = round.roundNumber,
                        trump = round.trump,
                        participantNames = state.participantNames,
                        scores = state.scoresByRound[round.id] ?: emptyList(),
                        onClick = { viewModel.startEditRound(round) }
                    )
                    HorizontalDivider()
                }
            }

            if (!isFinished) {
                RoundInputPanel(
                    participantNames = state.participantNames,
                    inputs = state.roundInputs,
                    selectedTrump = state.selectedTrump,
                    useZvanjeBela = game?.useZvanjeBela ?: false,
                    canSubmit = viewModel.canSubmitRound(),
                    onTrumpSelected = viewModel::selectTrump,
                    onBaseChange = viewModel::updateBasePoints,
                    onZvanjeToggle = viewModel::toggleZvanje,
                    onBelaToggle = viewModel::toggleBela,
                    onSubmit = viewModel::submitRound
                )
            }
        }
    }
}

@Composable
private fun RoundInputPanel(
    participantNames: List<String>,
    inputs: List<ParticipantScoreInput>,
    selectedTrump: TrumpSuit?,
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
