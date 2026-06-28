package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.SocialRepository
import ca.uwaterloo.kartingroyale.domain.model.SocialUser

class GetAllFollowings(private val socialRepository: SocialRepository) {
    suspend fun execute(): List<SocialUser> {
        return socialRepository.getAllFollowings()
    }
}

