package com.roomie.belottracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameMode { ONE_V_ONE, ONE_V_ONE_V_ONE, TWO_V_TWO }
enum class GameStatus { ONGOING, FINISHED }

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val mode: GameMode,
    val targetScore: Int,
    val useZvanjeBela: Boolean,
    val status: GameStatus = GameStatus.ONGOING,
    val participantNames: List<String>,
    val winnerName: String? = null,
    val totals: List<Int> = listOf()
)
