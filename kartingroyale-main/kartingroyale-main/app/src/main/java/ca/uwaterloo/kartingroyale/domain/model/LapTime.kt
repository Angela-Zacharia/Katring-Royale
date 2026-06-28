package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LogEntryDB(
    val id: String? = null,
    val user_id: String,
    val track_name: String,
    val entry_date: String,
    val race_date: String,
    val session_type: Int
)

@Serializable
data class TrackDB(
    val id: String? = null,
    val name: String,
    val description: String,
    val picture_url: String
)

@Serializable
data class LapTimeDB(
    val id: String? = null,
    val log_entry_id: String,
    val nth_lap: Int,
    val lap_time: Double
)