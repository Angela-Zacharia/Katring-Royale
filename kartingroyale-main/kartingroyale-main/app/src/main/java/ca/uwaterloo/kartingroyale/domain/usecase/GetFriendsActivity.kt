package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.SocialRepository
import ca.uwaterloo.kartingroyale.domain.model.FriendActivity

class GetFriendsActivity(private val socialRepository: SocialRepository) {
    suspend fun execute(): List<FriendActivity> {
        return socialRepository.getFriendsActivity()
    }
}

