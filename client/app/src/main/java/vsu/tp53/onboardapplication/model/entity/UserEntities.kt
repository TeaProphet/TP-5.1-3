package vsu.tp53.onboardapplication.model.entity

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable
import vsu.tp53.onboardapplication.model.domain.Token
import vsu.tp53.onboardapplication.model.domain.User

@Serializable
data class UserRegisterResponse(
    @JsonProperty("error")
    var error: String? = null,
    @JsonProperty("nickname")
    var nickname: String = "",
    @JsonProperty("login")
    var login: String = "",
    @JsonProperty("password")
    var password: String = ""
) {
    fun mapToDomain() = User(error, nickname, login, password)
}

@Serializable
data class UserRegisterPost(
    @JsonProperty("nickname")
    var nickname: String? = "",
    @JsonProperty("login")
    var login: String = "",
    @JsonProperty("password")
    var password: String = ""
)

@Serializable
data class UserAuthorize(
    @JsonProperty("login")
    var login: String = "",
    @JsonProperty("password")
    var password: String = ""
)

@Serializable
data class UserTokenResponse(
    @JsonProperty("error")
    var error: String? = null,
    @JsonProperty("nickname")
    var nickname: String = "",
    @JsonProperty("idToken")
    var idToken: String = ""
) {
    fun mapToDomain() = Token(error, nickname, idToken)
}

@Serializable
data class UserTokenPost(
    @JsonProperty("idToken")
    var idToken: String = ""
)