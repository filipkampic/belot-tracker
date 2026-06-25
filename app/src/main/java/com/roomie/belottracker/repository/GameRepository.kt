package com.roomie.belottracker.repository

import com.roomie.belottracker.data.dao.GameDao
import com.roomie.belottracker.data.dao.GameWithRounds
import com.roomie.belottracker.data.dao.RoundDao
import com.roomie.belottracker.data.dao.ScoreDao
import com.roomie.belottracker.data.entities.Game
import com.roomie.belottracker.data.entities.GameStatus
import com.roomie.belottracker.data.entities.Round
import com.roomie.belottracker.data.entities.Score
import com.roomie.belottracker.data.entities.TrumpSuit
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameDao: GameDao,
    private val roundDao: RoundDao,
    private val scoreDao: ScoreDao
) {

    suspend fun insertGame(game: Game) = gameDao.insert(game)

    suspend fun updateGame(game: Game) = gameDao.update(game)

    suspend fun deleteGame(game: Game) = gameDao.delete(game)

    suspend fun getGameById(id: Long): Game? = gameDao.getGameById(id)

    fun getAllGames(): Flow<List<Game>> = gameDao.getAllGames()

    fun getGamesByStatus(status: GameStatus): Flow<List<Game>> = gameDao.getGamesByStatus(status)

    suspend fun getGameWithRounds(gameId: Long): GameWithRounds? = gameDao.getGameWithRounds(gameId)

    suspend fun addRound(gameId: Long, roundNumber: Int, trump: TrumpSuit, scores: List<Score>) {
        val round = Round(
            gameId = gameId,
            roundNumber = roundNumber,
            trump = trump
        )
        val roundId = roundDao.insert(round)

        for (score in scores) {
            val updatedScore = score.copy(roundId = roundId)
            scoreDao.insert(updatedScore)
        }
    }

    suspend fun editRound(round: Round, scores: List<Score>) {
        roundDao.update(round)
        scoreDao.deleteScoresForRound(round.id)
        for (score in scores) {
            scoreDao.insert(score)
        }
    }

    suspend fun finishGame(gameId: Long, winnerParticipantId: Long) {
        val game = gameDao.getGameById(gameId) ?: return
        val updatedGame = game.copy(
            status = GameStatus.FINISHED,
            winnerParticipantId = winnerParticipantId
        )
        gameDao.update(updatedGame)
    }
}
