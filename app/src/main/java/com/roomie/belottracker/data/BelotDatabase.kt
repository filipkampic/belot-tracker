package com.roomie.belottracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.roomie.belottracker.data.dao.GameDao
import com.roomie.belottracker.data.dao.RoundDao
import com.roomie.belottracker.data.dao.ScoreDao
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.Round
import com.roomie.belottracker.data.entities.Score

@Database(
    entities = [Game::class, Round::class, Score::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BelotDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun roundDao(): RoundDao
    abstract fun scoreDao(): ScoreDao
}
