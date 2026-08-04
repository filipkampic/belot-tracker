package com.roomie.belottracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.belottracker.ui.viewmodel.HistoryViewModel
import com.roomie.belottracker.util.formatGameDate
import com.roomie.belottracker.util.modeLabel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onGameClick: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val games by viewModel.filteredGames.collectAsState()
    val players by viewModel.allPlayers.collectAsState()
    val selectedPlayerName by viewModel.selectedPlayerName.collectAsState()
    val grouped = games.groupBy { formatGameDate(it.date) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Povijest igara") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedPlayerName == null,
                        onClick = { viewModel.selectPlayerFilter(null) },
                        label = { Text("Svi") }
                    )
                }
                items(players) { playerName ->
                    FilterChip(
                        selected = selectedPlayerName == playerName,
                        onClick = { viewModel.selectPlayerFilter(playerName) },
                        label = { Text(playerName) }
                    )
                }
            }

            if (games.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nema završenih igara.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (date, gamesForDate) ->
                        stickyHeader {
                            Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        }
                        items(gamesForDate) { game ->
                            Card(
                                onClick = { onGameClick(game.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(modeLabel(game.mode), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
