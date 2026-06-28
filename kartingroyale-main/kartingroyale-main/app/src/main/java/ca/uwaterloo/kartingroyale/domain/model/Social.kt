package ca.uwaterloo.kartingroyale.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SocialUser(
    val username: String,
    val avatar: String? = null,
    val name: String,
    var isFollowing: Boolean,
)
