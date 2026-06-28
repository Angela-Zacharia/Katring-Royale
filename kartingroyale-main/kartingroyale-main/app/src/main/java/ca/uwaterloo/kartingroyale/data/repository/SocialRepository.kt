package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.domain.model.FriendActivity
import ca.uwaterloo.kartingroyale.domain.model.SocialUser

interface SocialRepository {
    suspend fun getAllFollowings(): List<SocialUser>
    suspend fun searchUsers(query: String): List<SocialUser>
    suspend fun followUser(username: String): Boolean
    suspend fun unfollowUser(username: String): Boolean
    suspend fun getFriendsActivity(): List<FriendActivity>
}