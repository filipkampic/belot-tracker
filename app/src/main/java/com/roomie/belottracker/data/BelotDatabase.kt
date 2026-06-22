package com.roomie.belottracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.Player
import com.roomie.belottracker.data.entities.Round
import com.roomie.belottracker.data.entities.Score
import com.roomie.belottracker.data.entities.Team

@Database(
    entities = [Player::class, Team::class, Game::class, Round::class, Score::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class BelotDatabase : RoomDatabase() {

}
