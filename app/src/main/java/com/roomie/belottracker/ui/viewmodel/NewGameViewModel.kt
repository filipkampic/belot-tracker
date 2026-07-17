package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.Player
import com.roomie.belottracker.repository.GameRepository
import com.roomie.belottracker.repository.PlayerRepository
import dagger.hilt.android.AndroidEntryPoint
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
    val selectedPlayerIds: List<Long> = emptyList(),
    val allPlayers: List<Player> = emptyList()
)

@HiltViewModel
class NewGameViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewGameUiState())
    val uiState: StateFlow<NewGameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepository.getAllPlayers().collect { players ->
                _uiState.value = _uiState.value.copy(allPlayers = players)
            }
        }
    }

    fun selectMode(mode: GameMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            selectedPlayerIds = emptyList(),
            targetScore = if (mode == GameMode.ONE_V_ONE) 501 else 1001
        )
    }

    fun selectTargetScore(score: Int) {
        _uiState.value = _uiState.value.copy(targetScore = score)
    }

    fun toggleZvanjeBela(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(useZvanjeBela = enabled)
    }

    fun requiredPlayerCount(): Int {
        return when (_uiState.value.mode) {
            GameMode.ONE_V_ONE -> 2
            GameMode.ONE_V_ONE_V_ONE -> 3
            GameMode.TWO_V_TWO -> 4
        }
    }

    fun togglePlayerSelection(playerId: Long) {
        val current = _uiState.value.selectedPlayerIds
        val max = requiredPlayerCount()
        val updated = when {
            current.contains(playerId) -> current - playerId
            current.size < max -> current + playerId
            else -> current
        }

        _uiState.value = _uiState.value.copy(selectedPlayerIds = updated)
    }

    fun addNewPlayer(playerName: String, onCreated: (Long) -> Unit) {
        if (playerName.isBlank()) return
        viewModelScope.launch {
            val id = playerRepository.insertPlayer(Player(name = playerName.trim()))
            onCreated(id)
        }
    }

    fun canStartGame(): Boolean = _uiState.value.selectedPlayerIds.size == requiredPlayerCount()

    fun createGame(onGameCreated: (Long) -> Unit) {
        val state = _uiState.value
        if (!canStartGame()) return
        viewModelScope.launch {
            val namesById = state.allPlayers.associate { it.id to it.name }
            val gameId = gameRepository.createGame(
                mode = state.mode,
                targetScore = state.targetScore,
                useZvanjeBela = state.useZvanjeBela,
                selectedPlayerIds = state.selectedPlayerIds,
                playerNamesById = namesById
            )
            onGameCreated(gameId)
        }
    }
}
