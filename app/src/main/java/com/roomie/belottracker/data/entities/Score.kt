package com.roomie.belottracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scores",
    foreignKeys = [ForeignKey(entity = Round::class, parentColumns = ["id"], childColumns = ["roundId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("roundId")]
)
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long,
    val participantIndex: Int,
    val basePoints: Int,
    val zvanjeEvents: List<String> = emptyList(),
    val bela: Boolean = false
)