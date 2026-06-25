package com.roomie.belottracker.util

import com.roomie.belottracker.data.entities.Score

fun Score.total(): Int {
    val zvanjeSum = zvanjeEvents.sumOf { it.toInt() }
    val belaBonus = if (bela) 20 else 0
    return basePoints + zvanjeSum + belaBonus
}

fun calculateGameTotals(scores: List<Score>): Map<Long, Int> {
    val gameTotals = mutableMapOf<Long, Int>()
    for (score in scores) {
        val participantId = score.participantId
        val total = gameTotals.getOrDefault(participantId, 0) + score.total()
        gameTotals[participantId] = total
    }

    return gameTotals
}

fun checkWinner(totals: Map<Long, Int>, targetScore: Int): Long? {
    val leader = totals.maxByOrNull { it.value } ?: return null
    return if (leader.value >= targetScore) leader.key else null
}
