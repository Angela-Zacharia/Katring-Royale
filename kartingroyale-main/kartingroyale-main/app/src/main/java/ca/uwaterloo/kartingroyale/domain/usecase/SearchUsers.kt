package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.SocialRepository
import ca.uwaterloo.kartingroyale.domain.model.SocialUser

class SearchUsers(private val socialRepository: SocialRepository) {
    suspend fun execute(query: String): List<SocialUser> {
        return socialRepository.searchUsers(query)
    }
}

