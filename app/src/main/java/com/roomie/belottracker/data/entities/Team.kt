package com.roomie.belottracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "teams",
    foreignKeys = [
        ForeignKey(entity = Player::class, parentColumns = ["id"], childColumns = ["player1Id"]),
        ForeignKey(entity = Player::class, parentColumns = ["id"], childColumns = ["player2Id"])
    ]
)
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val player1Id: Long,
    val player2Id: Long,
    val name: String
)
