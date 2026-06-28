package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.SocialRepository
import ca.uwaterloo.kartingroyale.data.repository.UserRepository

class UpdateUserName(private val userRepository: UserRepository) {
    suspend fun execute(newUsername: String): Unit {
        return userRepository.updateUserName(newUsername)
    }
}


