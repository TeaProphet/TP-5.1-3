package vsu.tp53.onboardapplication.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

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

@Serializable
data class User(
    var error: String?,
    var nickname: String?,
    var login: String,
    var password: String
) {
    fun mapToUserRegEntity() = UserRegisterPost(nickname, login, password)
    fun mapToUserAuthEntity() = UserAuthorize(login, password)
}

@Serializable
data class Token(
    var error: String?,
    var nickname: String,
    var idToken: String
) {
    fun mapToEntity() = UserTokenPost(idToken)
}

data class UserLogInInfo(
    val nickname: String?,
    val login: String,
    val tokenId: String,
    val expire: LocalDateTime
)