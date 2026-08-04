package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.repository.GameRepository
import com.roomie.belottracker.util.calculateGameTotals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WinnerUiState(val winnerName: String, val finalScore: Int)

@HiltViewModel
class WinnerViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val gameId: Long = savedStateHandle.get<Long>("gameId") ?: 0L
    private val winnerName: String = savedStateHandle.get<String>("winnerName") ?: ""

    private val _uiState = MutableStateFlow<WinnerUiState?>(null)
    val uiState: StateFlow<WinnerUiState?> = _uiState

    init {
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId) ?: return@launch
            val rounds = gameRepository.getGameWithRounds(gameId)?.rounds ?: emptyList()
            val allScores = rounds.flatMap { gameRepository.getScoresForRound(it.id) }
            val totals = calculateGameTotals(allScores, game.mode)
            val finalScore = totals.values.maxOrNull() ?: 0

            _uiState.value = WinnerUiState(
                winnerName = winnerName,
                finalScore = finalScore
            )
        }
    }
}
