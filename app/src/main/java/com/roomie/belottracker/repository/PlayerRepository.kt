package com.roomie.belottracker.repository

import com.roomie.belottracker.data.dao.PlayerDao
import com.roomie.belottracker.data.entities.Player
import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val playerDao: PlayerDao) {

    suspend fun insertPlayer(player: Player) = playerDao.insert(player)

    suspend fun updatePlayer(player: Player) = playerDao.update(player)

    suspend fun deletePlayer(player: Player) = playerDao.delete(player)

    fun getAllPlayers(): Flow<List<Player>> = playerDao.getAllPlayers()

    suspend fun getPlayerById(id: Long): Player? = playerDao.getPlayerById(id)
}
