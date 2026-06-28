package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecentRaces(
    val previewUrl: String? = null,
    val trackName: String,
    val bestTime: String,
    val daysAgo: Int,
)