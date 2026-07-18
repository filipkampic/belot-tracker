package com.roomie.belottracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.Score
import com.roomie.belottracker.data.entities.TrumpSuit
import com.roomie.belottracker.util.Participant
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
fun SuitPicker(selectedSuid: TrumpSuit?, onSuitSelected: (TrumpSuit) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TrumpSuit.entries.forEach { suit ->
            FilterChip(
                selected = selectedSuid == suit,
                onClick = { onSuitSelected(suit) },
                label = { Text(suit.displayName) }
            )
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
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("20", "50", "100").forEach { value ->
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
fun RoundRow(
    roundNumber: Int,
    trump: TrumpSuit,
    participants: List<Participant>,
    scores: List<Score>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(roundNumber.toString(), style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(28.dp))
        Text(trump.displayName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(72.dp))
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
            participants.forEach { p ->
                val total = scores.find { it.participantId == p.id }?.total() ?: 0
                Text(total.toString(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
