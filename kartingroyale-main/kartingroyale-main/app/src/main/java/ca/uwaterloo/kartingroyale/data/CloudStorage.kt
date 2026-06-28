package ca.uwaterloo.kartingroyale.data

import ca.uwaterloo.kartingroyale.domain.model.FriendActivity
import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry
import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces
import android.os.Build
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import androidx.annotation.RequiresApi
import ca.uwaterloo.kartingroyale.domain.model.LapTimeDB
import ca.uwaterloo.kartingroyale.domain.model.LogEntryDB
import ca.uwaterloo.kartingroyale.domain.model.RaceType
import ca.uwaterloo.kartingroyale.domain.model.SocialUser
import ca.uwaterloo.kartingroyale.domain.model.TrackDB
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * CloudStorage class: save to Supabase instance w/ hard-coded credentials (for now)
 */

class CloudStorage : IDatabase {
    var supabase: SupabaseClient? = null
    var auth: Auth? = null

    init {
        supabase = createSupabaseClient(
            supabaseUrl = "https://dybipfvwnkossqwturim.supabase.co",
            supabaseKey = "sb_publishable_MaIMf8hz4KoxbfEyVb-ivg_CA0B15JI"
        ) {
            install(Postgrest)
            install(Auth)
        }
        auth = supabase?.auth
    }

    /*
     * Auth methods
     */
    suspend fun createUser(email: String, password: String, displayName: String) {
        supabase?.auth?.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val userId = supabase?.auth?.currentUserOrNull()?.id
        if (userId != null) {
            supabase?.from("Users")?.upsert(
                mapOf(
                    "id" to userId,
                    "username" to email,
                    "name" to displayName
                )
            )
        }
    }

    suspend fun loginUser(email: String, password: String) {
        supabase?.auth?.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logoutUser() {
        supabase?.auth?.signOut()
    }

    /*
     * IDatabase table methods
     */

    override suspend fun addLogEntry(userId: String, entry: LogEntry) {
        println("Calling LogViewModel")
        if (userId.isEmpty()) {
            println("User ID is empty. Cannot add log entry.")
            return
        }

        var track = supabase?.from("Track")?.select {
                filter { eq("name", entry.trackName) }
            }?.decodeList<TrackDB>()?.firstOrNull()

        track = track ?: supabase?.from("Track")?.insert(
                TrackDB(name = entry.trackName, description = "", picture_url = "")
            ) {
                select()
            }?.decodeSingleOrNull<TrackDB>()
        val trackId = track?.id ?: throw Exception("Could not find or create track")

        val logEntryDB = LogEntryDB(
            user_id = userId,
            track_name = trackId,
            session_type = entry.raceType.ordinal,
            race_date = entry.date,
            entry_date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        print("adding log entry")
        println(entry)
        println(userId)

        val newEntry = supabase?.from("LogEntry")?.insert(logEntryDB) {
            select()
        }?.decodeSingleOrNull<LogEntryDB>()

        val entryId = newEntry?.id ?: throw Exception("Could not find or create log entry")

        if (entryId != null) {
            val laps = entry.lapTimes.mapIndexed { index, time ->
                LapTimeDB(
                    log_entry_id = entryId,
                    nth_lap = index + 1,
                    lap_time = time
                )
            }

            if (laps.isNotEmpty()) {
                supabase?.from("LapTimes")?.insert(laps)
            }
        }
        println("added log entry")
    }

    override suspend fun getAllLogEntries(userId: String): List<LogEntry> {
        if (userId == "") {
            return emptyList()
        }

        val response = supabase?.from("LogEntry")
            ?.select(columns = Columns.raw("*, LapTimes(*), Track(*)")) {
                filter {
                    eq("user_id", userId)
                }
            }

        val logEntries = mutableListOf<LogEntry>()
        for (res in response?.decodeList<JsonObject>() ?: emptyList()) {
            val lapTimes = mutableListOf<Double>()

            res["LapTimes"]?.jsonArray?.forEach { lap ->
                val time = lap.jsonObject["lap_time"]?.jsonPrimitive?.doubleOrNull
                if (time != null) {
                    lapTimes.add(time)
                }
            }

            val rawRaceType = res["session_type"]?.jsonPrimitive?.int ?: 0
            val raceType = RaceType.entries[rawRaceType]
            val trackName = res["Track"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Unknown"

            val entry = LogEntry(
                id = res["id"]?.jsonPrimitive?.content ?: "",
                trackName = trackName,
                date = res["race_date"]?.jsonPrimitive?.content ?: "",
                raceType = raceType,
                lapTimes = lapTimes
            )

            logEntries.add(entry)
        }

        return logEntries
    }

    override suspend fun editLogEntry(entryId: String, entry: LogEntry) {
        try {
            if (entryId.isBlank()) {
                println("Entry ID is blank")
                return
            }
            val formattedDate = entry.date.take(10)

            var track = supabase?.from("Track")?.select {
                filter { eq("name", entry.trackName.trim()) }
            }?.decodeList<TrackDB>()?.firstOrNull()

            track = track ?: supabase?.from("Track")?.insert(
                TrackDB(
                    name = entry.trackName.trim(),
                    description = "",
                    picture_url = ""
                )
            ) {
                select()
            }?.decodeSingleOrNull<TrackDB>()

            val trackId = track?.id
            if (trackId == null) {
                return
            }

            val updateData = buildJsonObject {
                put("track_name", trackId)
                put("session_type", entry.raceType.ordinal)
                put("race_date", formattedDate)
            }

            val updateResponse = supabase?.from("LogEntry")?.update(updateData) {
                filter { eq("id", entryId) }
                select()
            }

            supabase?.from("LapTimes")?.delete {
                filter { eq("log_entry_id", entryId) }
            }
            
            val validLaps = entry.lapTimes
                .filter { it > 0 }
                .mapIndexed { index, time ->
                    buildJsonObject {
                        put("log_entry_id", entryId)
                        put("nth_lap", index + 1)
                        put("lap_time", time)
                    }
                }

            if (validLaps.isNotEmpty()) {
                supabase?.from("LapTimes")?.insert(validLaps)
            }

        } catch (e: Exception) {
            println(" EDIT FAILED: ${e.message}")
            e.printStackTrace()
        }
    }

    override suspend fun deleteLogEntry(entryId: String) {
        supabase?.from("LogEntry")?.delete {
            filter {
                eq("id", entryId)
            }
        }
    }

    override suspend fun getUserName(): String {
        println("getting stuff uwu")
        val id = supabase?.auth?.currentUserOrNull()?.id ?: return "User"
        println("Getting username for $id")

        return try {
            val userJson = supabase?.from("Users")?.select {
                filter {
                    eq("id", id)
                }
            }?.decodeSingleOrNull<JsonObject>()

            println("json name ${userJson?.get("name")?.jsonPrimitive?.contentOrNull }")
            userJson?.get("name")?.jsonPrimitive?.contentOrNull ?: "User"

        } catch (e: Exception) {
            println("Getting username failed with error ${e.message}")
            "User"
        }
    }

    suspend fun getTrackImageUrl(trackName: String): String? {
        return try {
            val track = supabase?.from("Track")?.select {
                filter { eq("name", trackName) }
            }?.decodeSingleOrNull<TrackDB>()
            track?.picture_url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getRecentRaces(): List<RecentRaces> {
        return try {
            val userId = supabase?.auth?.currentUserOrNull()?.id ?: return emptyList()

            val response = supabase?.from("LogEntry")
                ?.select(columns = Columns.raw("*, LapTimes(*), Track(*)")) {
                    filter { eq("user_id", userId) }
                    order("race_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(5)
                }

            val todayMs = System.currentTimeMillis()
            val msPerDay = 86_400_000L
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            response?.decodeList<JsonObject>()?.mapNotNull { res ->
                val lapTimes = res["LapTimes"]?.jsonArray
                    ?.mapNotNull { it.jsonObject["lap_time"]?.jsonPrimitive?.doubleOrNull }
                    ?: emptyList()

                val bestLap = lapTimes.minOrNull() ?: return@mapNotNull null
                val bestTimeStr = "%.3f".format(bestLap) + "s"

                val dateStr = res["race_date"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val raceMs = sdf.parse(dateStr)?.time ?: return@mapNotNull null
                val daysAgo = ((todayMs - raceMs) / msPerDay).toInt().coerceAtLeast(0)

                val trackObj = res["Track"]?.jsonObject
                val trackName = trackObj?.get("name")?.jsonPrimitive?.contentOrNull ?: "Unknown"
                val previewUrl = trackObj?.get("picture_url")?.jsonPrimitive?.contentOrNull
                    ?.takeIf { it.isNotBlank() }

                RecentRaces(
                    previewUrl = previewUrl,
                    trackName = trackName,
                    bestTime = bestTimeStr,
                    daysAgo = daysAgo
                )
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFriendsActivity(): List<FriendActivity> {
        return try {
            supabase!!.postgrest
                .rpc("get_friends_activity")
                .decodeList<FriendActivity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFollowings(): List<SocialUser> {
        try {
            print("Request to get all followings")
            return supabase!!.postgrest.rpc("get_followings").decodeList<SocialUser>()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun searchUsers(searchQuery: String): List<SocialUser> {
        val params = buildJsonObject {
            put("search_query", searchQuery)
        }

        try {
            return supabase!!.postgrest.rpc(
                function = "search_users",
                parameters = params
            ).decodeList<SocialUser>()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun followUser(username: String): Boolean {
        val params = buildJsonObject {
            put("target_username", username)
        }

        try {
            return supabase!!.postgrest.rpc(
                "follow_user_by_username",
                parameters = params
            ).decodeAs<Boolean>()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun unfollowUser(username: String): Boolean {
        val params = buildJsonObject {
            put("target_username", username)
        }

        try {
            return supabase!!.postgrest.rpc(
                "unfollow_user_by_username",
                parameters = params
            ).decodeAs<Boolean>()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun getTracks(): List<String> {
        return try {
            supabase!!.from("Track")
                .select(Columns.raw("name"))
                .decodeList<JsonObject>()
                .mapNotNull { it["name"]?.jsonPrimitive?.contentOrNull }
                .distinct()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getLeaderboard(
        track: String,
        isGlobal: Boolean
    ): List<LeaderboardEntry> {
        return try {

            return supabase!!.postgrest.rpc(
                function = "get_track_leaderboard",
                parameters = buildJsonObject {
                    put("track_name_input", track);
                    put("is_global", isGlobal)
                }
            ).decodeList<LeaderboardEntry>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updateUserName(newUserName: String): Unit {
        val userId = supabase?.auth?.currentUserOrNull()?.id ?: throw IllegalStateException("User doesn't exist")

        supabase?.from("Users")?.update(
            {
                // The column name in DB -> The new value
                set("name", newUserName)
            }
        ) {
            filter {
                eq("id", userId)
            }
        }
    }
}
