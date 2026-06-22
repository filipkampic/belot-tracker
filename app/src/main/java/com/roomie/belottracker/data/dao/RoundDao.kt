package com.roomie.belottracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.roomie.belottracker.data.entities.Round
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Insert
    suspend fun insert(round: Round): Long

    @Update
    suspend fun update(round: Round)

    @Delete
    suspend fun delete(round: Round)

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber ASC")
    fun getRoundsForGame(gameId: Long): Flow<List<Round>>

    @Query("SELECT * FROM rounds WHERE id = :id")
    suspend fun getRoundById(id: Long): Round?
}
