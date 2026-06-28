package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.data.IDatabase
import ca.uwaterloo.kartingroyale.data.MockDatabase
import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces

class LeaderboardRepositoryImpl(private val db: IDatabase = CloudStorage()) :
    LeaderboardRepository {
    override suspend fun getUserName(): String {
        return db.getUserName()
    }

    override suspend fun getRecentRaces(): List<RecentRaces> {
        return db.getRecentRaces()
    }

    override suspend fun getTracks(): List<String> {
        return db.getTracks()
    }

    override suspend fun getLeaderboard(track: String, isGlobal: Boolean): List<LeaderboardEntry> {
        return db.getLeaderboard(track, isGlobal)
    }
}