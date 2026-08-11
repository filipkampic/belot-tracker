package com.roomie.belottracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.GameMode
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
    val isLoading: Boolean = true,
    val game: Game? = null,
    val participantNames: List<String> = emptyList(),
    val rounds: List<Round> = emptyList(),
    val scoresByRound: Map<Long, List<Score>> = emptyMap(),
    val totals: Map<Int, Int> = emptyMap(),
    val selectedTrump: TrumpSuit? = null,
    val trumpPickerIndex: Int? = null,
    val dealerIndex: Int? = null,
    val firstTrumpPickerIndex: Int? = null,
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
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId) ?: return@launch
            val rounds = gameRepository.getRounds(gameId)
            val playedRoundsCount = rounds.size

            val scoresByRound = mutableMapOf<Long, List<Score>>()
            val allScores = mutableListOf<Score>()
            for (round in rounds) {
                val scores = gameRepository.getScoresForRound(round.id)
                scoresByRound[round.id] = scores
                allScores.addAll(scores)
            }
            val totals = calculateGameTotals(allScores)

            val numberOfPlayers = game.participantNames.size
            val initialDealer = game.initialDealerIndex

            val dealerIndex: Int
            val firstTrumpPickerIndex: Int

            if (numberOfPlayers == 4) {
                val seatingOrder = listOf(0, 2, 1, 3)
                val initialSeat = seatingOrder.indexOf(initialDealer)

                val dealerSeat = (initialSeat + playedRoundsCount) % 4
                dealerIndex = seatingOrder[dealerSeat]

                val pickerSeat = (dealerSeat + 1) % 4
                firstTrumpPickerIndex = seatingOrder[pickerSeat]
            } else {
                dealerIndex = (initialDealer + playedRoundsCount) % numberOfPlayers
                firstTrumpPickerIndex = (dealerIndex + 1) % numberOfPlayers
            }

            val requiredInputsCount = if (game.mode == GameMode.TWO_V_TWO) 2 else game.participantNames.size

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                game = game,
                participantNames = game.participantNames,
                rounds = rounds,
                scoresByRound = scoresByRound,
                totals = totals,
                selectedTrump = null,
                dealerIndex = dealerIndex,
                firstTrumpPickerIndex = firstTrumpPickerIndex,
                trumpPickerIndex = firstTrumpPickerIndex,
                editingRound = null,
                roundInputs = (0 until requiredInputsCount).map { index ->
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
        if (state.selectedTrump == null) return false
        val requiredCount = if (state.game?.mode == GameMode.TWO_V_TWO) 2 else state.participantNames.size
        val activeInputs = state.roundInputs.take(requiredCount)
        return activeInputs.all { it.basePoints.toIntOrNull() != null }
    }

    fun submitRound() {
        val state = _uiState.value
        val trump = state.selectedTrump ?: return
        if (!canSubmitRound()) return

        viewModelScope.launch {
            val gameMode = state.game?.mode ?: GameMode.ONE_V_ONE

            val scores = if (gameMode == GameMode.TWO_V_TWO) {
                listOf(
                    Score(
                        roundId = 0,
                        participantIndex = 0,
                        basePoints = state.roundInputs.getOrNull(0)?.basePoints?.toIntOrNull() ?: 0,
                        zvanjeEvents = state.roundInputs.getOrNull(0)?.zvanjeEvents ?: emptyList(),
                        bela = state.roundInputs.getOrNull(0)?.bela ?: false
                    ),
                    Score(
                        roundId = 0,
                        participantIndex = 1,
                        basePoints = state.roundInputs.getOrNull(1)?.basePoints?.toIntOrNull() ?: 0,
                        zvanjeEvents = state.roundInputs.getOrNull(1)?.zvanjeEvents ?: emptyList(),
                        bela = state.roundInputs.getOrNull(1)?.bela ?: false
                    )
                )
            } else {
                state.roundInputs.map {
                    Score(
                        roundId = 0,
                        participantIndex = it.participantIndex,
                        basePoints = it.basePoints.toIntOrNull() ?: 0,
                        zvanjeEvents = it.zvanjeEvents,
                        bela = it.bela
                    )
                }
            }

            val nextRoundNumber = state.rounds.size + 1
            val picker = state.trumpPickerIndex ?: 0

            val numberOfPlayers = state.participantNames.size
            val initialDealer = state.game?.initialDealerIndex ?: 0
            val dealerIndex: Int
            val firstTrumpPickerIndex: Int

            if (numberOfPlayers == 4) {
                val seatingOrder = listOf(0, 2, 1, 3)
                val initialSeat = seatingOrder.indexOf(initialDealer)

                val dealerSeat = (initialSeat + nextRoundNumber - 1) % 4
                dealerIndex = seatingOrder[dealerSeat]

                val pickerSeat = (dealerSeat + 1) % 4
                firstTrumpPickerIndex = seatingOrder[pickerSeat]
            } else {
                dealerIndex = (initialDealer + nextRoundNumber - 1) % numberOfPlayers
                firstTrumpPickerIndex = (dealerIndex + 1) % numberOfPlayers
            }

            gameRepository.addRound(gameId, nextRoundNumber, trump, picker, dealerIndex, firstTrumpPickerIndex, scores)

            val updatedAllScores = state.scoresByRound.values.flatten() + scores
            val totals = calculateGameTotals(updatedAllScores, gameMode)
            val totalsCount = if (gameMode == GameMode.TWO_V_TWO) 2 else state.participantNames.size
            val totalsList = (0 until totalsCount).map { totals[it] ?: 0 }

            val currentGame = state.game
            if (currentGame != null) {
                val winnerIndex = checkWinner(totals, currentGame.targetScore, gameMode)
                if (winnerIndex != null) {
                    val winnerName = if (gameMode == GameMode.TWO_V_TWO) {
                        if (winnerIndex == 0) "Tim 1 (${currentGame.participantNames.getOrNull(0)} & ${currentGame.participantNames.getOrNull(1)})"
                        else "Tim 2 (${currentGame.participantNames.getOrNull(2)} & ${currentGame.participantNames.getOrNull(3)})"
                    } else {
                        currentGame.participantNames.getOrNull(winnerIndex)
                    }

                    if (winnerName != null) {
                        val updatedGame = currentGame.copy(
                            totals = totalsList,
                            winnerName = winnerName
                        )
                        gameRepository.updateGame(updatedGame)
                        gameRepository.finishGame(gameId, winnerName)
                        _navigateToWinner.send(winnerName)
                    }
                } else {
                    val updatedGame = currentGame.copy(totals = totalsList)
                    gameRepository.updateGame(updatedGame)
                }
                loadGame()
            }
        }
    }

    fun startEditRound(round: Round) {
        val scores = _uiState.value.scoresByRound[round.id] ?: emptyList()
        val gameMode = _uiState.value.game?.mode ?: GameMode.ONE_V_ONE

        val inputs = if (gameMode == GameMode.TWO_V_TWO) {
            val scoreTeam1 = scores.find { it.participantIndex == 0 }
            val scoreTeam2 = scores.find { it.participantIndex == 1 }
            listOf(
                ParticipantScoreInput(
                    participantIndex = 0,
                    basePoints = scoreTeam1?.basePoints?.toString() ?: "",
                    zvanjeEvents = scoreTeam1?.zvanjeEvents ?: emptyList(),
                    bela = scoreTeam1?.bela ?: false
                ),
                ParticipantScoreInput(
                    participantIndex = 1,
                    basePoints = scoreTeam2?.basePoints?.toString() ?: "",
                    zvanjeEvents = scoreTeam2?.zvanjeEvents ?: emptyList(),
                    bela = scoreTeam2?.bela ?: false
                )
            )
        } else {
            scores.map {
                ParticipantScoreInput(
                    participantIndex = it.participantIndex,
                    basePoints = it.basePoints.toString(),
                    zvanjeEvents = it.zvanjeEvents,
                    bela = it.bela
                )
            }
        }

        _uiState.value = _uiState.value.copy(
            editingRound = round,
            selectedTrump = round.trump,
            dealerIndex = round.dealerIndex,
            firstTrumpPickerIndex = round.firstTrumpPickerIndex,
            roundInputs = inputs
        )
    }

    fun cancelEditRound() {
        val state = _uiState.value
        val gameMode = state.game?.mode ?: GameMode.ONE_V_ONE
        val requiredCount = if (gameMode == GameMode.TWO_V_TWO) 2 else state.participantNames.size

        _uiState.value = state.copy(
            editingRound = null,
            selectedTrump = null,
            roundInputs = (0 until requiredCount).map { index ->
                ParticipantScoreInput(participantIndex = index)
            }
        )
    }

    fun submitEditRound() {
        val state = _uiState.value
        val round = state.editingRound ?: return
        val trump = state.selectedTrump ?: return
        val dealerIndex = state.dealerIndex ?: round.dealerIndex
        val firstTrumpPickerIndex = state.firstTrumpPickerIndex ?: round.firstTrumpPickerIndex

        viewModelScope.launch {
            val updatedRound = round.copy(
                trump = trump,
                dealerIndex = dealerIndex,
                firstTrumpPickerIndex = firstTrumpPickerIndex
            )
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

            val rounds = gameRepository.getRounds(gameId)
            val allScores = mutableListOf<Score>()
            for (r in rounds) {
                allScores.addAll(gameRepository.getScoresForRound(r.id))
            }

            val totals = calculateGameTotals(allScores)
            val totalsList = state.game?.participantNames?.indices?.map { totals[it] ?: 0 } ?: emptyList()

            val currentGame = state.game
            if (currentGame != null) {
                val updatedGame = currentGame.copy(totals = totalsList)
                gameRepository.updateGame(updatedGame)
            }

            loadGame()
        }
    }
}
