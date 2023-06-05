package vsu.tp53.onboardapplication.model.entity

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable
import org.codehaus.jackson.annotate.JsonCreator

@Serializable
data class ChangeReputationEntity(
    var idToken: String,
    var requestedNickname: String
)

@Serializable
data class ReputationEntity(
    var error: String? = null,
    var new_reputation: Double = 0.0
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
    var age: Int = 0,
    var games: String = "",
    var vk: String = "",
    var tg: String = "",
    var password: String? = null
)

@Serializable
data class ErrorEntity(
    var error: String = ""
)

@Serializable
data class ProfileInfoEntity @JsonCreator constructor(
    @JsonProperty("error") var error: String? = "",
    @JsonProperty("age") var age: Int? = 0,
    @JsonProperty("games") var games: String? = "",
    @JsonProperty("vk") var vk: String? = "",
    @JsonProperty("tg") var tg: String? = "",
    @JsonProperty("reputation") var reputation: Double = 0.0,
    @JsonProperty("played_sessions") var played_sessions: MutableList<Int>? = null,
    @JsonProperty("is_admin") var is_admin: Boolean,
    @JsonProperty("is_banned") var is_banned: Boolean
)

@Serializable
data class SearchProfile(
    var nickname: String = ""
)

@Serializable
data class ProfileBanEntity(
    var requestedNickname: String = "",
    var idToken: String = ""
)

@Serializable
data class ProfileBanEntityResponse(
    var error: String? = null,
    var requestedNickname: String = "",
    var idToken: String = ""
)