package com.roomie.belottracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TrumpSuit(val displayName: String) {
    ZIR("Žir"),
    LIST("List"),
    BUNDEVA("Bundeva"),
    SRCE("Srce")
}

@Entity(
    tableName = "rounds",
    foreignKeys = [ForeignKey(entity = Game::class, parentColumns = ["id"], childColumns = ["gameId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("gameId")]
)
data class Round(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val roundNumber: Int,
    val trump: TrumpSuit
)
