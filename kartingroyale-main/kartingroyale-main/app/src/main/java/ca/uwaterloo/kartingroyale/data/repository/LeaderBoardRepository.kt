package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces

interface LeaderboardRepository {

    suspend fun getRecentRaces(): List<RecentRaces>

    suspend fun getUserName(): String

    suspend fun getTracks(): List<String>

    suspend fun getLeaderboard(track: String, isGlobal: Boolean): List<LeaderboardEntry>
}