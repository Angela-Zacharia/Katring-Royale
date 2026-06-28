package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.domain.model.FriendActivity
import ca.uwaterloo.kartingroyale.domain.model.SocialUser

interface UserRepository {
    suspend fun updateUserName(newUserName: String): Unit
}