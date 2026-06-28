package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepository

class GetTracks(private val repository: LeaderboardRepository) {
    suspend fun execute(): List<String> {
        return repository.getTracks();
    }
}