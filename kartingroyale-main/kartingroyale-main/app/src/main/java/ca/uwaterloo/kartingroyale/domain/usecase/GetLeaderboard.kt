package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepository
import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry

class GetLeaderboard(private val repository: LeaderboardRepository) {
    suspend fun execute(trackName: String, isGlobal: Boolean): List<LeaderboardEntry> {
        return repository.getLeaderboard(trackName, isGlobal);
    }
}