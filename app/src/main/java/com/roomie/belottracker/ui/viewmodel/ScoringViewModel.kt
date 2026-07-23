package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.Round
import com.roomie.belottracker.data.entities.Score
import com.roomie.belottracker.data.entities.TrumpSuit
import com.roomie.belottracker.repository.GameRepository
import com.roomie.belottracker.util.calculateGameTotals
import com.roomie.belottracker.util.checkWinner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParticipantScoreInput(
    val participantIndex: Int,
    val basePoints: String = "",
    val zvanjeEvents: List<String> = emptyList(),
    val bela: Boolean = false
)

data class ScoringUiState(
    val game: Game? = null,
    val participantNames: List<String> = emptyList(),
    val rounds: List<Round> = emptyList(),
    val scoresByRound: Map<Long, List<Score>> = emptyMap(),
    val totals: Map<Int, Int> = emptyMap(),
    val selectedTrump: TrumpSuit? = null,
    val trumpPickerIndex: Int? = null,
    val roundInputs: List<ParticipantScoreInput> = emptyList(),
    val editingRound: Round? = null
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gameId: Long = savedStateHandle.get<Long>("gameId") ?: 0L

    private val _uiState = MutableStateFlow(ScoringUiState())
    val uiState: StateFlow<ScoringUiState> = _uiState.asStateFlow()

    private val _navigateToWinner = Channel<String>(Channel.BUFFERED)
    val navigateToWinner = _navigateToWinner.receiveAsFlow()

    init { loadGame() }

    private fun loadGame() {
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId) ?: return@launch
            val gameWithRounds = gameRepository.getGameWithRounds(gameId)
            val rounds = gameWithRounds?.rounds?.sortedBy { it.roundNumber } ?: emptyList()

            val scoresByRound = mutableMapOf<Long, List<Score>>()
            val allScores = mutableListOf<Score>()
            for (round in rounds) {
                val scores = gameRepository.getScoresForRound(round.id)
                scoresByRound[round.id] = scores
                allScores.addAll(scores)
            }
            val totals = calculateGameTotals(allScores)

            _uiState.value = _uiState.value.copy(
                game = game,
                participantNames = game.participantNames,
                rounds = rounds,
                scoresByRound =scoresByRound,
                totals = totals,
                selectedTrump = null,
                editingRound = null,
                roundInputs = game.participantNames.mapIndexed { index, _ ->
                    ParticipantScoreInput(participantIndex = index)
                }
            )
        }
    }

    fun selectTrump(suit: TrumpSuit) {
        _uiState.value = _uiState.value.copy(selectedTrump = suit)
    }

    fun setTrumpPickerIndex(index: Int) {
        _uiState.value = _uiState.value.copy(trumpPickerIndex = index)
    }

    fun updateBasePoints(participantIndex: Int, value: String) {
        val updated = _uiState.value.roundInputs.map {
            if (it.participantIndex == participantIndex) it.copy(basePoints = value) else it
        }
        _uiState.value = _uiState.value.copy(roundInputs = updated)
    }

    fun toggleZvanje(participantIndex: Int, value: String) {
        val updated = _uiState.value.roundInputs.map {
            if (it.participantIndex == participantIndex) {
                val events = if (it.zvanjeEvents.contains(value)) it.zvanjeEvents - value else it.zvanjeEvents + value
                it.copy(zvanjeEvents = events)
            } else it
        }
        _uiState.value = _uiState.value.copy(roundInputs = updated)
    }

    fun toggleBela(participantIndex: Int) {
        val updated = _uiState.value.roundInputs.map {
            if (it.participantIndex == participantIndex) it.copy(bela = !it.bela) else it
        }
        _uiState.value = _uiState.value.copy(roundInputs = updated)
    }

    fun canSubmitRound(): Boolean {
        val state = _uiState.value
        return state.selectedTrump != null && state.roundInputs.all { it.basePoints.toIntOrNull() != null }
    }

    fun submitRound() {
        val state = _uiState.value
        val trump = state.selectedTrump ?: return
        if (!canSubmitRound()) return

        viewModelScope.launch {
            val scores = state.roundInputs.map {
                Score(
                    roundId = 0,
                    participantIndex = it.participantIndex,
                    basePoints = it.basePoints.toIntOrNull() ?: 0,
                    zvanjeEvents = it.zvanjeEvents,
                    bela = it.bela
                )
            }
            val nextRoundNumber = state.rounds.size + 1
            val picker = state.trumpPickerIndex ?: 0
            gameRepository.addRound(gameId, nextRoundNumber, trump, picker, scores)

            val updatedAllScores = state.scoresByRound.values.flatten() + scores
            val totals = calculateGameTotals(updatedAllScores)
            val winnerIndex = checkWinner(totals, state.game?.targetScore ?: Int.MAX_VALUE)

            if (winnerIndex != null) {
                val winnerName = state.game?.participantNames?.get(winnerIndex)
                if (winnerName != null) {
                    gameRepository.finishGame(gameId, winnerName)
                    _navigateToWinner.send(winnerName)
                }
            }
            loadGame()
        }
    }

    fun startEditRound(round: Round) {
        val scores = _uiState.value.scoresByRound[round.id] ?: emptyList()
        val inputs = scores.map {
            ParticipantScoreInput(
                participantIndex = it.participantIndex,
                basePoints = it.basePoints.toString(),
                zvanjeEvents = it.zvanjeEvents,
                bela = it.bela
            )
        }
        _uiState.value = _uiState.value.copy(editingRound = round, selectedTrump = round.trump, roundInputs = inputs)
    }

    fun cancelEditRound() {
        val participantNames = _uiState.value.participantNames
        _uiState.value = _uiState.value.copy(
            editingRound = null,
            selectedTrump = null,
            roundInputs = participantNames.mapIndexed { index, _ ->
                ParticipantScoreInput(participantIndex = index)
            }
        )
    }

    fun submitEditRound() {
        val state = _uiState.value
        val round = state.editingRound ?: return
        val trump = state.selectedTrump ?: return

        viewModelScope.launch {
            val updatedRound = round.copy(trump = trump)
            val scores = state.roundInputs.map {
                Score(
                    roundId = round.id,
                    participantIndex = it.participantIndex,
                    basePoints = it.basePoints.toIntOrNull() ?: 0,
                    zvanjeEvents = it.zvanjeEvents,
                    bela = it.bela
                )
            }
            gameRepository.editRound(updatedRound, scores)
            loadGame()
        }
    }
}
