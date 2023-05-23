package vsu.tp53.onboardapplication.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class ChangeReputationEntity(
    var idToken: String,
    var address_login: String
)

@Serializable
data class ReputationEntity(
    var error: String? = null,
    var new_reputation: Double
)

@Serializable
data class ChangeProfileEntity(
    var idToken: String,
    var age: Int,
    var games: String,
    var vk: String,
    var tg: String,
    var password: String? = null
)

@Serializable
data class ChangeProfile(
    var age: Int,
    var games: String,
    var vk: String,
    var tg: String,
    var password: String? = null
)

@Serializable
data class ErrorEntity(
    var error: String = ""
)

@Serializable
data class ProfileInfoEntity(
    var age: Int? = 0,
    var games: String? = "",
    var vk: String? = "",
    var tg: String? = "",
    var played_sessions: Map<Int, SessionBody>? = null
)

@Serializable
data class SearchProfile(
    var searched_login: String = ""
)