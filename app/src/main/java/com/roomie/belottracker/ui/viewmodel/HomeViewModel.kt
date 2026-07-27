package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.repository.GameRepository
import com.roomie.belottracker.util.calculateGameTotals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class GameSummary(
    val game: Game,
    val totals: Map<Int, Int>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {
    val recentGames: StateFlow<List<GameSummary>> = combine(
        gameRepository.getAllGames(),
        gameRepository.getAllGameScores()
    ) { games, gameScores ->
        val scoresByGame = gameScores.groupBy({ it.gameId }, { it.score })
        games.take(5).map { game ->
            GameSummary(
                game = game,
                totals = calculateGameTotals(scoresByGame[game.id] ?: emptyList())
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
