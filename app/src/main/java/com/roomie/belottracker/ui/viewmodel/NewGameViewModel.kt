package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewGameUiState(
    val mode: GameMode = GameMode.ONE_V_ONE,
    val targetScore: Int = 501,
    val useZvanjeBela: Boolean = false,
    val players: List<String> = emptyList()
)

@HiltViewModel
class NewGameViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewGameUiState())
    val uiState: StateFlow<NewGameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val lastGame = gameRepository.getLastGame()
            if (lastGame != null) {
                _uiState.value = _uiState.value.copy(
                    players = lastGame.participantNames.toMutableList(),
                )
            }
        }
    }

    fun selectMode(mode: GameMode) {
        val count = requiredPlayerCount(mode)
        _uiState.value = _uiState.value.copy(
            mode = mode,
            players = List(count) { "" },
            targetScore = if (mode == GameMode.ONE_V_ONE) 501 else 1001
        )
    }

    fun selectTargetScore(score: Int) {
        _uiState.value = _uiState.value.copy(targetScore = score)
    }

    fun toggleZvanjeBela(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(useZvanjeBela = enabled)
    }

    fun requiredPlayerCount(mode: GameMode = _uiState.value.mode): Int {
        return when (mode) {
            GameMode.ONE_V_ONE -> 2
            GameMode.ONE_V_ONE_V_ONE -> 3
            GameMode.TWO_V_TWO -> 4
        }
    }

    fun editPlayer(index: Int, newName: String) {
        val updated = _uiState.value.players.toMutableList()
        while (updated.size <= index) updated.add("")
        updated[index] = newName
        _uiState.value = _uiState.value.copy(players = updated)
    }

    fun canStartGame(): Boolean = _uiState.value.players.count { it.isNotBlank() } == requiredPlayerCount()

    fun createGame(onGameCreated: (Long) -> Unit) {
        val state = _uiState.value
        if (!canStartGame()) return
        viewModelScope.launch {
            val numberOfPlayers = state.players.size
            val initialDealer = (0 until numberOfPlayers).random()

            val gameId = gameRepository.createGame(
                mode = state.mode,
                targetScore = state.targetScore,
                useZvanjeBela = state.useZvanjeBela,
                players = state.players,
                initialDealer = initialDealer
            )
            onGameCreated(gameId)
        }
    }
}
