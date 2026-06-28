package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val id: String = "",
    val trackName: String,
    val date: String,
    val raceType: RaceType,
    val lapTimes: List<Double>,
    val notes: String = ""
)