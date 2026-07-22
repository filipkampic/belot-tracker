package com.roomie.belottracker.ui.viewmodel

import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.Round
import com.roomie.belottracker.data.entities.Score
import com.roomie.belottracker.data.entities.TrumpSuit

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
    val roundInputs: List<ParticipantScoreInput> = emptyList(),
    val editingRound: Round? = null
)