package com.roomie.belottracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.annotation.DrawableRes
import com.roomie.belottracker.R

enum class TrumpSuit(
    val displayName: String,
    @DrawableRes val iconRes: Int
) {
    ZIR("Žir", R.drawable.zir),
    LIST("List", R.drawable.list),
    BUNDEVA("Bundeva", R.drawable.bundeva),
    SRCE("Srce", R.drawable.srce)
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
    val trump: TrumpSuit,
    val trumpPickerIndex: Int
)
