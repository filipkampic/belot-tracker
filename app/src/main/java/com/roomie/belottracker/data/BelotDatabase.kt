package com.roomie.belottracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.roomie.belottracker.data.dao.GameDao
import com.roomie.belottracker.data.dao.PlayerDao
import com.roomie.belottracker.data.dao.RoundDao
import com.roomie.belottracker.data.dao.ScoreDao
import com.roomie.belottracker.data.dao.TeamDao
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.Player
import com.roomie.belottracker.data.entities.Round
import com.roomie.belottracker.data.entities.Score
import com.roomie.belottracker.data.entities.Team

@Database(
    entities = [Player::class, Team::class, Game::class, Round::class, Score::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BelotDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun gameDao(): GameDao
    abstract fun roundDao(): RoundDao
    abstract fun scoreDao(): ScoreDao
}
