package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RaceType {
    RACE, QUALIFYING, PRACTICE
}