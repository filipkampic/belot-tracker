package com.roomie.belottracker.data

import androidx.room.TypeConverter
import com.roomie.belottracker.data.entities.GameMode
import com.roomie.belottracker.data.entities.GameStatus
import com.roomie.belottracker.data.entities.TrumpSuit

class Converters {
    @TypeConverter fun fromZvanjeList(list: List<String>): String = list.joinToString(",")
    @TypeConverter fun toZvanjeList(data: String): List<String> = if (data.isEmpty()) emptyList() else data.split(",")

    @TypeConverter fun fromGameMode(m: GameMode): String = m.name
    @TypeConverter fun toGameMode(s: String): GameMode = GameMode.valueOf(s)

    @TypeConverter fun fromGameStatus(s: GameStatus): String = s.name
    @TypeConverter fun toGameStatus(s: String): GameStatus = GameStatus.valueOf(s)

    @TypeConverter fun fromTrumpSuit(t: TrumpSuit): String = t.name
    @TypeConverter fun toTrumpSuit(s: String): TrumpSuit = TrumpSuit.valueOf(s)

    @TypeConverter fun fromLongList(list: List<Long>): String = list.joinToString(",")
    @TypeConverter fun toLongList(data: String): List<Long> = if (data.isEmpty()) emptyList() else data.split(",").map { it.toLong() }
}
