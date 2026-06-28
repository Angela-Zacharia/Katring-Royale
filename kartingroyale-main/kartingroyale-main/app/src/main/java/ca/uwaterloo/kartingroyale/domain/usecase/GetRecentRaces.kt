package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepository
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces

class GetRecentRaces(private val repository: LeaderboardRepository) {

    suspend fun execute(): List<RecentRaces> {
        return repository.getRecentRaces()
    }
}