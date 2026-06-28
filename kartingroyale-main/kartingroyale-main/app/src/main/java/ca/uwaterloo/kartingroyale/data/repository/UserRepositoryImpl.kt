package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.data.IDatabase
import ca.uwaterloo.kartingroyale.data.MockDatabase
import ca.uwaterloo.kartingroyale.domain.model.FriendActivity

class UserRepositoryImpl() : UserRepository {
    private val db = CloudStorage()
    override suspend fun updateUserName(newUserName: String) = db.updateUserName(newUserName)
}