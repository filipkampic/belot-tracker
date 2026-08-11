package com.roomie.belottracker.ui.screens

import android.R.attr.navigationIcon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.GameStatus
import com.roomie.belottracker.data.entities.Round
import com.roomie.belottracker.data.entities.TrumpSuit
import com.roomie.belottracker.ui.components.LoadingScreen
import com.roomie.belottracker.ui.components.PrimaryButton
import com.roomie.belottracker.ui.components.RoundRow
import com.roomie.belottracker.ui.components.SuitPicker
import com.roomie.belottracker.ui.components.TotalsCard
import com.roomie.belottracker.ui.components.ZvanjeChipRow
import com.roomie.belottracker.ui.viewmodel.ParticipantScoreInput
import com.roomie.belottracker.ui.viewmodel.ScoringViewModel
import com.roomie.belottracker.util.modeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    gameId: Long,
    onBack: () -> Unit,
    onWinner: (Long, String) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        LoadingScreen()
        return
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToWinner.collect { winnerIndex -> onWinner(gameId, winnerIndex) }
    }

    val game = state.game
    val isFinished = game?.status == GameStatus.FINISHED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = game?.let { modeLabel(it.mode) } ?: "Rezultati",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        val subTitle = if (isFinished) "Igra je završena" else "Cilj: ${game?.targetScore ?: 1001} bodova"
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TotalsCard(
                    participantNames = state.participantNames,
                    totals = state.totals,
                    mode = game?.mode ?: GameMode.ONE_V_ONE,
                    targetScore = game?.targetScore ?: 1001,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            items(state.rounds) { round ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    RoundRow(
                        roundNumber = round.roundNumber,
                        trump = round.trump,
                        trumpPickerIndex = round.trumpPickerIndex,
                        participantNames = state.participantNames,
                        scores = state.scoresByRound[round.id] ?: emptyList(),
                        onClick = { viewModel.startEditRound(round) }
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }

            if (!isFinished) {
                item {
                    val dealerName = state.dealerIndex?.let { state.participantNames[it] }
                    val firstPickerName =
                        state.firstTrumpPickerIndex?.let { state.participantNames[it] }

                    if (dealerName != null && firstPickerName != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Dijeli:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                    Text(
                                        text = dealerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Bira adut:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                    Text(
                                        text = firstPickerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    RoundInputPanel(
                        gameMode = game?.mode ?: GameMode.ONE_V_ONE,
                        participantNames = state.participantNames,
                        inputs = state.roundInputs,
                        selectedTrump = state.selectedTrump,
                        trumpPickerIndex = state.trumpPickerIndex,
                        onPickerSelected = { viewModel.setTrumpPickerIndex(it) },
                        useZvanjeBela = game?.useZvanjeBela ?: false,
                        canSubmit = viewModel.canSubmitRound(),
                        editingRound = state.editingRound,
                        onTrumpSelected = { suit -> viewModel.selectTrump(suit) },
                        onBaseChange = viewModel::updateBasePoints,
                        onZvanjeToggle = viewModel::toggleZvanje,
                        onBelaToggle = viewModel::toggleBela,
                        onSubmit = viewModel::submitRound,
                        onSubmitEdit = viewModel::submitEditRound
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundInputPanel(
    gameMode: GameMode,
    participantNames: List<String>,
    inputs: List<ParticipantScoreInput>,
    selectedTrump: TrumpSuit?,
    trumpPickerIndex: Int?,
    onPickerSelected: (Int) -> Unit,
    useZvanjeBela: Boolean,
    canSubmit: Boolean,
    editingRound: Round?,
    onTrumpSelected: (TrumpSuit) -> Unit,
    onBaseChange: (Int, String) -> Unit,
    onZvanjeToggle: (Int, String) -> Unit,
    onBelaToggle: (Int) -> Unit,
    onSubmit: () -> Unit,
    onSubmitEdit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (editingRound != null) "Uredi rundu #${editingRound.roundNumber}" else "Unos nove runde",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Odaberi adut:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SuitPicker(
                        selectedSuit = selectedTrump,
                        onSuitSelected = onTrumpSelected
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                Text(
                    "Tko je odabrao adut?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = trumpPickerIndex?.let { participantNames.getOrNull(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Igrač") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        participantNames.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    onPickerSelected(index)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            val teamIndices = if (gameMode == GameMode.TWO_V_TWO) listOf(0, 1) else inputs.indices.toList()

            teamIndices.forEach { index ->
                val input = inputs.getOrNull(index) ?: ParticipantScoreInput(participantIndex = index)

                val title = if (gameMode == GameMode.TWO_V_TWO) {
                    val p1 = participantNames.getOrNull(0) ?: "Igrač 1"
                    val p2 = participantNames.getOrNull(1) ?: "Igrač 2"
                    val p3 = participantNames.getOrNull(2) ?: "Igrač 3"
                    val p4 = participantNames.getOrNull(3) ?: "Igrač 4"
                    if (index == 0) "Tim 1 ($p1 & $p2)" else "Tim 2 ($p3 & $p4)"
                } else {
                    participantNames.getOrNull(index) ?: "Igrač ${index + 1}"
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = input.basePoints,
                            onValueChange = { onBaseChange(input.participantIndex, it) },
                            label = { Text("Bodovi u igri") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (useZvanjeBela) {
                            Text(
                                text = "Zvanja i Bela:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            ZvanjeChipRow(
                                selectedEvents = input.zvanjeEvents,
                                onToggle = { zvanje ->
                                    onZvanjeToggle(
                                        input.participantIndex,
                                        zvanje
                                    )
                                },
                                belaSelected = input.bela,
                                onBelaToggle = { onBelaToggle(input.participantIndex) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            PrimaryButton(
                text = if (editingRound != null) "Spremi izmjene" else "Dodaj rundu",
                onClick = { if (editingRound != null) onSubmitEdit() else onSubmit() },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
