package com.roomie.belottracker.util

import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.Score

fun Score.total(): Int {
    val zvanjeSum = zvanjeEvents.sumOf { event ->
        when (event) {
            "Štigla" -> 90
            else -> event.toIntOrNull() ?: 0
        }
    }
    val belaBonus = if (bela) 20 else 0
    return basePoints + zvanjeSum + belaBonus
}

fun calculateGameTotals(scores: List<Score>, mode: GameMode = GameMode.ONE_V_ONE): Map<Int, Int> {
    val gameTotals = mutableMapOf<Int, Int>()
    for (score in scores) {
        val participantIndex = score.participantIndex
        val total = gameTotals.getOrDefault(participantIndex, 0) + score.total()
        gameTotals[participantIndex] = total
    }

    return gameTotals
}

fun checkWinner(totals: Map<Int, Int>, targetScore: Int, mode: GameMode = GameMode.ONE_V_ONE): Int? {
    if (mode == GameMode.TWO_V_TWO) {
        val team1Score = totals[0] ?: 0
        val team2Score = totals[1] ?: 0
        return when {
            team1Score >= targetScore && team1Score > team2Score -> 0
            team2Score >= targetScore && team2Score > team1Score -> 1
            else -> null
        }
    }

    val leader = totals.maxByOrNull { it.value } ?: return null
    return if (leader.value >= targetScore) leader.key else null
}
