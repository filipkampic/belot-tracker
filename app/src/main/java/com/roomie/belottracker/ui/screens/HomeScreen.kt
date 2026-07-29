package com.roomie.belottracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.GameStatus
import com.roomie.belottracker.ui.viewmodel.HomeViewModel
import com.roomie.belottracker.util.formatGameDate
import com.roomie.belottracker.util.modeLabel

@Preview(showBackground = false)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onNewGameClick = {}, onHistoryClick = {}, onGameClick = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewGameClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onGameClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentGames by viewModel.recentGames.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Belot Tracker") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewGameClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nova igra") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nedavne igre")
                TextButton(onClick = onHistoryClick) { Text("Sva povijest") }
            }

            if (recentGames.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Još nema igara.\nPritisni 'Nova igra' za početak.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentGames) { game ->
                        Card(
                            onClick = { onGameClick(game.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    formatGameDate(game.date),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        modeLabel(game.mode),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        if (game.status == GameStatus.FINISHED) "Završeno" else "U tijeku",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(4.dp))

                                val resultText = when(game.mode) {
                                    GameMode.ONE_V_ONE -> {
                                        val p = game.participantNames
                                        val t = game.totals
                                        val name1 = p.getOrNull(0) ?: ""
                                        val name2 = p.getOrNull(1) ?: ""
                                        val score1 = t.getOrNull(0) ?: 0
                                        val score2 = t.getOrNull(1) ?: 0
                                        "$name1 $score1 : $score2 $name2"
                                    }
                                    GameMode.ONE_V_ONE_V_ONE -> {
                                        game.participantNames.mapIndexed { index, name ->
                                            "$name: ${game.totals.getOrNull(index) ?: 0}"
                                        }.joinToString(" · ")
                                    }
                                    GameMode.TWO_V_TWO -> {
                                        val t = game.totals
                                        val team1 = (t.getOrNull(0) ?: 0) + (t.getOrNull(1) ?: 0)
                                        val team2 = (t.getOrNull(2) ?: 0) + (t.getOrNull(3) ?: 0)
                                        "$team1 : $team2"
                                    }
                                }
                                Text(
                                    text = resultText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
