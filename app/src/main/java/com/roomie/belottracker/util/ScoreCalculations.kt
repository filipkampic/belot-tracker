package com.roomie.belottracker.util

import com.roomie.belottracker.data.entities.Score

fun Score.total(): Int {
    val zvanjeSum = zvanjeEvents.sumOf { it.toInt() }
    val belaBonus = if (bela) 20 else 0
    return basePoints + zvanjeSum + belaBonus
}
