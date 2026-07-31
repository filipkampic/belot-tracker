package com.roomie.belottracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.Score
import com.roomie.belottracker.data.entities.TrumpSuit
import com.roomie.belottracker.util.total

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun ScoreInputField(label: String, value: String, onValueChange: (String) -> Unit,modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> if (new.all { it.isDigit() }) onValueChange(new) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier
    )
}

@Composable
fun ModeSegmentedControl(selectedMode: GameMode, onModeSelected: (GameMode) -> Unit, modifier: Modifier = Modifier) {
    val modes = listOf(GameMode.ONE_V_ONE, GameMode.ONE_V_ONE_V_ONE, GameMode.TWO_V_TWO)
    val labels = listOf("1v1", "1v1v1", "2v2")
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        modes.forEachIndexed { i, mode ->
            SegmentedButton(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = modes.size),
                icon = { }
            ) { Text(labels[i]) }
        }
    }
}

@Composable
private fun SuitChip(
    suit: TrumpSuit,
    selectedSuit: TrumpSuit?,
    onSuitSelected: (TrumpSuit) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selectedSuit == suit,
        onClick = { onSuitSelected(suit) },
        label = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = suit.iconRes),
                    contentDescription = suit.displayName,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = modifier.height(40.dp)
    )
}

@Composable
fun SuitPicker(
    selectedSuit: TrumpSuit?,
    onSuitSelected: (TrumpSuit) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuitChip(TrumpSuit.ZIR, selectedSuit, onSuitSelected, modifier = Modifier.weight(1f))
            SuitChip( TrumpSuit.LIST, selectedSuit, onSuitSelected, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuitChip(TrumpSuit.BUNDEVA, selectedSuit, onSuitSelected, modifier = Modifier.weight(1f))
            SuitChip(TrumpSuit.SRCE, selectedSuit, onSuitSelected, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ZvanjeChipRow(
    selectedEvents: List<String>,
    onToggle: (String) -> Unit,
    belaSelected: Boolean,
    onBelaToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("20", "50", "100", "150", "200", "Štiglja").forEach { value ->
            FilterChip(
                selected = selectedEvents.contains(value),
                onClick = { onToggle(value) },
                label = { Text(value) }
            )
        }
        FilterChip(
            selected = belaSelected,
            onClick = onBelaToggle,
            label = { Text("Bela") }
        )
    }
}

@Composable
fun TotalsCard(participantNames: List<String>, totals: Map<Int, Int>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            participantNames.forEachIndexed { index, name ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(name, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    Text(
                        (totals[index] ?: 0).toString(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RoundRow(
    roundNumber: Int,
    trump: TrumpSuit,
    trumpPickerIndex: Int,
    participantNames: List<String>,
    scores: List<Score>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(roundNumber.toString(), style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(28.dp))
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                painter = painterResource(id = trump.iconRes),
                contentDescription = trump.displayName,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(participantNames[trumpPickerIndex], style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
            participantNames.forEachIndexed { index, _ ->
                val total = scores.find { it.participantIndex == index }?.total() ?: 0
                Text(total.toString(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
