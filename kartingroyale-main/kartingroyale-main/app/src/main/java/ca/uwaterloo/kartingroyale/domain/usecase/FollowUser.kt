package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.SocialRepository

class FollowUser(private val socialRepository: SocialRepository) {
    suspend fun execute(username: String): Boolean {
        return socialRepository.followUser(username)
    }
}

