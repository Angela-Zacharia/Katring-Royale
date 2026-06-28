package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionSummary(
    val trackName: String,
    val date: String,
    val raceType: RaceType,
    val bestLap: Double,
    val averageLap: Double,
    val totalLaps: Int
)
