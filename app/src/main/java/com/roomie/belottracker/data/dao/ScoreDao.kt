package com.roomie.belottracker.data.dao

import androidx.room.*
import com.roomie.belottracker.data.entities.Score
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM scores WHERE roundId = :roundId")
    suspend fun deleteScoresForRound(roundId: Long)
}
