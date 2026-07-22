package com.roomie.belottracker.util

import com.roomie.belottracker.data.entities.Score

fun Score.total(): Int {
    val zvanjeSum = zvanjeEvents.sumOf { it.toInt() }
    val belaBonus = if (bela) 20 else 0
    return basePoints + zvanjeSum + belaBonus
}

fun calculateGameTotals(scores: List<Score>): Map<Int, Int> {
    val gameTotals = mutableMapOf<Int, Int>()
    for (score in scores) {
        val participantIndex = score.participantIndex
        val total = gameTotals.getOrDefault(participantIndex, 0) + score.total()
        gameTotals[participantIndex] = total
    }

    return gameTotals
}

fun checkWinner(totals: Map<Int, Int>, targetScore: Int): Int? {
    val leader = totals.maxByOrNull { it.value } ?: return null
    return if (leader.value >= targetScore) leader.key else null
}
