package com.roomie.belottracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.roomie.belottracker.data.entities.Team

@Dao
interface TeamDao {
    @Insert
    suspend fun insert(team: Team): Long

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getTeamById(id: Long): Team?

    @Query("SELECT * FROM teams WHERE player1Id = :playerId OR player2Id = :playerId")
    suspend fun getTeamsForPlayer(playerId: Long): List<Team>
}
