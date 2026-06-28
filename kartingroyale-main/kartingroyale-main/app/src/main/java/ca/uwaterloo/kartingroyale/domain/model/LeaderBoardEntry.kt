package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val player: String,
    val laptime: String
)