package com.roomie.belottracker.data.dao

import androidx.room.*
import com.roomie.belottracker.data.entities.Score
import kotlinx.coroutines.flow.Flow

data class GameScore(
    val gameId: Long,
    @Embedded val score: Score
)

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: Score): Long

    @Update
    suspend fun update(score: Score)

    @Delete
    suspend fun delete(score: Score)

    @Query("SELECT * FROM scores WHERE roundId = :roundId")
    suspend fun getScoresForRound(roundId: Long): List<Score>

    @Query("SELECT * FROM scores WHERE roundId IN (SELECT id FROM rounds WHERE gameId = :gameId)")
    fun getScoresForGame(gameId: Long): Flow<List<Score>>

    @Query(
        "SELECT rounds.gameId AS gameId, scores.* FROM scores " +
            "INNER JOIN rounds ON scores.roundId = rounds.id"
    )
    fun getAllGameScores(): Flow<List<GameScore>>

    @Query("DELETE FROM scores WHERE roundId = :roundId")
    suspend fun deleteScoresForRound(roundId: Long)
}
