package ca.uwaterloo.kartingroyale.data

import ca.uwaterloo.kartingroyale.domain.model.FriendActivity
import ca.uwaterloo.kartingroyale.domain.model.LapEntry
import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry
import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces
import ca.uwaterloo.kartingroyale.domain.model.SocialUser
import kotlinx.serialization.json.Json

class MockDatabase : IDatabase {

    private val logEntries = mutableListOf<LogEntry>()

    fun initialize() {
        if (logEntries.isNotEmpty()) return

        val rawEntries = getRawLogEntries()
        rawEntries.forEach { jsonString ->
            val entry = Json.decodeFromString<LogEntry>(jsonString)
            logEntries.add(entry)
        }
    }

    fun getRawLogEntries(): List<String> {
        return listOf(
            """{"trackName": "K1 Speed Cambridge", "date": "2025-02-10", "raceType": "PRACTICE", "lapTimes": [20.02, 19.70, 19.12, 19.45, 18.62], "notes": "Freezing Track conditions, Tire struggles to warm up"}""",
            """{"trackName": "K1 Speed Cambridge", "date": "2025-02-11", "raceType": "PRACTICE", "lapTimes": [18.0, 18.02, 18.20, 19.45, 18.62], "notes": "Freezing Track conditions, Tire struggles to warm up"}""",
            """{"trackName": "SilverStone Circuit", "date": "2025-02-08", "raceType": "RACE", "lapTimes": [90.27, 92.33, 91.45, 93.33, 94.21], "notes": "MAX VERSTAPPEN, F1 British GP 2023"}""",
            """{"trackName": "Macau Street Circuit", "date": "2025-02-05", "raceType": "QUALIFYING", "lapTimes": [120.90, 120.75, 119.60, 118.55], "notes": "Macau GP"}"""
        )
    }

    override suspend fun getUserName(): String {
        return "Thomas"
    }

    override suspend fun getRecentRaces(): List<RecentRaces> {
        return listOf(
            RecentRaces(
                trackName = "K1 Speed Cambridge",
                previewUrl = "https://motorsporttickets.com/blog/wp-content/uploads/2024/03/USA-Stats-1024x573.png",
                bestTime = "17.90s",
                daysAgo = 2
            ),
            RecentRaces(
                trackName = "Yas Marina Circuit",
                previewUrl = null, // Not present in the second JSON string
                bestTime = "1:30:27",
                daysAgo = 63
            ),
            RecentRaces(
                trackName = "Lusail International Circuit",
                previewUrl = null, // Not present in the third JSON string
                bestTime = "1:40:71",
                daysAgo = 71
            )
        )
    }

    override suspend fun getFriendsActivity(): List<FriendActivity> {
        return listOf(
            FriendActivity(
                name = "Max Verstappen",
                avatar = "https://img2.51gt3.com/rac/racer/202404/736683032ce24f06ba67ce44a2cf0b73.jpg",
                trackName = "Bahrain International Circuit",
                bestTime = "01:34.798",
                laps = listOf(
                    LapEntry(lap = 1, time = "01:36.120"),
                    LapEntry(lap = 2, time = "01:35.340"),
                    LapEntry(lap = 3, time = "01:34.798"),
                ),
                daysAgo = 2
            ),
            FriendActivity(
                name = "George Russell",
                avatar = "https://img2.51gt3.com/rac/racer/202503/f10f01a1704147ca90ab3a4325f38785.png",
                trackName = "Bahrain International Circuit",
                bestTime = "01:36.108",
                laps = listOf(
                    LapEntry(lap = 1, time = "01:38.200"),
                    LapEntry(lap = 2, time = "01:36.108"),
                ),
                daysAgo = 2
            )
        )
    }

    private val allUsers = listOf(
        SocialUser(name = "Jane Doe", username = "firebird123", isFollowing = true),
        SocialUser(name = "John Smith", username = "user1", isFollowing = false),
        SocialUser(name = "Alex Lee", username = "user2", isFollowing = true),
        SocialUser(name = "Sarah Connor1", username = "user3", isFollowing = false),
        SocialUser(name = "Sarah Connor2", username = "user31", isFollowing = false),
        SocialUser(name = "Sarah Connor3", username = "user32", isFollowing = false),
        SocialUser(name = "Sarah Connor4", username = "user33", isFollowing = false),
        SocialUser(name = "Sarah Connor5", username = "user34", isFollowing = false),
        SocialUser(name = "Sarah Connor6", username = "user35", isFollowing = false),
        SocialUser(name = "Sarah Connor7", username = "user36", isFollowing = false),
        SocialUser(name = "Sarah Connor8", username = "user37", isFollowing = false),
        SocialUser(name = "Max Verstappen", username = "max", isFollowing = true),
        SocialUser(name = "George Russell", username = "george", isFollowing = true)
    )

    override suspend fun getFollowings(): List<SocialUser> {
        return allUsers.filter { it.isFollowing }
    }

    override suspend fun searchUsers(searchQuery: String): List<SocialUser> {
        return allUsers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    override suspend fun followUser(username: String): Boolean {
        val user = allUsers.find { it.username == username }
        if (user != null) {
            user.isFollowing = true
            return true
        }
        return false
    }

    override suspend fun unfollowUser(username: String): Boolean {
        val user = allUsers.find { it.username == username }
        if (user != null) {
            user.isFollowing = false
            return true
        }
        return false
    }

    private var nextId = 1

    override suspend fun addLogEntry(userId: String, entry: LogEntry) {
        logEntries.add(entry.copy(id = "${nextId++}"))
    }

    override suspend fun getAllLogEntries(userId: String): List<LogEntry> {
        return logEntries.toList()
    }

    override suspend fun editLogEntry(entryId: String, entry: LogEntry) {
        val index = logEntries.indexOfFirst { it.id == entryId }
        if (index != -1) {
            logEntries[index] = entry
        }
    }

    override suspend fun deleteLogEntry(entryId: String) {
        logEntries.removeAll { it.id == entryId }
    }

    override suspend fun updateUserName(newUserName: String) {
    }

    override suspend fun getTracks(): List<String> {
        return listOf(
            "K1 Speed Cambridge",
            "Yas Marina Circuit",
            "Lusail International Circuit",
            "Bahrain International Circuit"
        )

    }

    override suspend fun getLeaderboard(track: String, isGlobal: Boolean): List<LeaderboardEntry> {
        return listOf(
            LeaderboardEntry(
                rank = 1,
                player = "Thomas",
                laptime = "1:40:71"
            )
        )
    }
}