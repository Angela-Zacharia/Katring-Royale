package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LapEntry(
    val lap: Int,
    val time: String,
)

@Serializable
data class FriendActivity(
    val avatar: String? = null,
    val name: String,
    val trackName: String,
    val bestTime: String,
    val laps: List<LapEntry> = emptyList(),
    val daysAgo: Int,
)
