package com.roomie.belottracker.util

import com.roomie.belottracker.data.entities.GameMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatGameDate(epochMillis: Long): String =
    SimpleDateFormat("d.M.yyyy.", Locale("hr", "HR")).format(Date(epochMillis))

fun modeLabel(mode: GameMode): String = when (mode) {
    GameMode.ONE_V_ONE -> "1v1"
    GameMode.ONE_V_ONE_V_ONE -> "1v1v1"
    GameMode.TWO_V_TWO -> "2v2"
}

fun targetScoreOptions(mode: GameMode): List<Int> = when (mode) {
    GameMode.ONE_V_ONE -> listOf(301, 501, 701)
    GameMode.ONE_V_ONE_V_ONE -> listOf(501, 701, 1001)
    else -> listOf(701, 1001, 1501)
}
