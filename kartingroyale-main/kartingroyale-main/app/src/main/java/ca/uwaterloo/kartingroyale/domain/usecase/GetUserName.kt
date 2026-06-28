package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepository

class GetUserName(private val repository: LeaderboardRepository) {

    suspend fun execute(): String {
        return repository.getUserName()
    }
}