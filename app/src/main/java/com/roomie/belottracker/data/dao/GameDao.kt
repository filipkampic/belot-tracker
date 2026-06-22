package com.roomie.belottracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.GameStatus
import com.roomie.belottracker.data.entities.Round
import kotlinx.coroutines.flow.Flow

data class GameWithRounds(
    @Embedded val game: Game,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val rounds: List<Round>
)

@Dao
interface GameDao {
    @Insert
    suspend fun insert(game: Game): Long

    @Update
    suspend fun update(game: Game)

    @Delete
    suspend fun delete(game: Game)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Long): Game?

    @Query("SELECT * FROM games ORDER BY date DESC")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE status = :status ORDER BY date DESC")
    fun getGamesByStatus(status: GameStatus): Flow<List<Game>>

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameWithRounds(gameId: Long): GameWithRounds?
}
