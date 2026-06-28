package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.data.IDatabase
import ca.uwaterloo.kartingroyale.data.MockDatabase
import ca.uwaterloo.kartingroyale.domain.model.FriendActivity

class SocialRepositoryImpl(private val db: IDatabase = CloudStorage()) : SocialRepository {
    override suspend fun getAllFollowings() = db.getFollowings()
    override suspend fun searchUsers(query: String) = db.searchUsers(query)
    override suspend fun followUser(username: String) = db.followUser(username)
    override suspend fun unfollowUser(username: String) = db.unfollowUser(username)
    override suspend fun getFriendsActivity(): List<FriendActivity> {
        return db.getFriendsActivity()
    }


}