package com.roomie.belottracker.repository

import com.roomie.belottracker.data.dao.TeamDao
import com.roomie.belottracker.data.entities.Team

class TeamRepository(private val teamDao: TeamDao) {
    suspend fun insertTeam(team: Team): Long = teamDao.insert(team)
    suspend fun getTeamById(id: Long): Team? = teamDao.getTeamById(id)
    suspend fun getTeamsForPlayer(playerId: Long): List<Team> = teamDao.getTeamsForPlayer(playerId)
}
