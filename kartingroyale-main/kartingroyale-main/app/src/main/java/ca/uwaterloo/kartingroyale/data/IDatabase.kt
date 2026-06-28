package ca.uwaterloo.kartingroyale.data

import ca.uwaterloo.kartingroyale.domain.model.FriendActivity
import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry
import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces
import ca.uwaterloo.kartingroyale.domain.model.SocialUser

interface IDatabase {
    // Log entry operations
    suspend fun addLogEntry(userId: String, entry: LogEntry)
    suspend fun getAllLogEntries(userId: String): List<LogEntry>
    suspend fun editLogEntry(entryId: String, entry: LogEntry)
    suspend fun deleteLogEntry(entryId: String)

    // User info
    suspend fun getUserName(): String

    // Recent races
    suspend fun getRecentRaces(): List<RecentRaces>

    // Friends activity
    suspend fun getFriendsActivity(): List<FriendActivity>

    // Social operations
    suspend fun getFollowings(): List<SocialUser>
    suspend fun searchUsers(searchQuery: String): List<SocialUser>
    suspend fun followUser(username: String): Boolean
    suspend fun unfollowUser(username: String): Boolean

    suspend fun getTracks(): List<String>
    suspend fun getLeaderboard(track: String, isGlobal: Boolean): List<LeaderboardEntry>

    suspend fun updateUserName(newUserName: String): Unit

}