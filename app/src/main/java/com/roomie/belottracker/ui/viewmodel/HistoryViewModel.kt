package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.GameStatus
import com.roomie.belottracker.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {
    val allPlayers: StateFlow<List<String>> = gameRepository.getGamesByStatus(GameStatus.FINISHED)
        .map { games ->
            games.flatMap { it.participantNames }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlayerName = MutableStateFlow<String?>(null)
    val selectedPlayerName: StateFlow<String?> = _selectedPlayerName.asStateFlow()

    private val _filteredGames = MutableStateFlow<List<Game>>(emptyList())
    val filteredGames: StateFlow<List<Game>> = _filteredGames.asStateFlow()

    init {
        viewModelScope.launch {
            gameRepository.getGamesByStatus(GameStatus.FINISHED).collect { games ->
                applyFilter(games, _selectedPlayerName.value)
            }
        }
    }

    fun selectPlayerFilter(playerName: String?) {
        _selectedPlayerName.value = playerName
        viewModelScope.launch {
            val games = gameRepository.getGamesByStatus(GameStatus.FINISHED).first()
            applyFilter(games, playerName)
        }
    }

    private fun applyFilter(games: List<Game>, playerName: String?) {
        _filteredGames.value = if (playerName.isNullOrBlank()) {
            games
        } else {
            games.filter { game ->
                game.participantNames.any { name ->
                    name.equals(playerName, ignoreCase = true)
                }
            }
        }
    }
}
