package vsu.tp53.onboardapplication.model.entity

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable
import vsu.tp53.onboardapplication.model.domain.Token
import vsu.tp53.onboardapplication.model.domain.User

@Serializable
data class UserRegAuthorize(
    @JsonProperty("error")
    var error: String? = null,
    @JsonProperty("login")
    var login: String = "",
    @JsonProperty("password")
    var password: String = ""
) {
    fun mapToDomain() = User(login, password)
}

@Serializable
data class UserRegAuthorizePost(
    @JsonProperty("login")
    var login: String = "",
    @JsonProperty("password")
    var password: String = ""
)

@Serializable
data class UserToken(
    @JsonProperty("error")
    var error: String? = null,
    @JsonProperty("idToken")
    var idToken: String = ""
) {
    fun mapToDomain() = Token(idToken)
}

@Serializable
data class UserTokenPost(
    @JsonProperty("idToken")
    var idToken: String = ""
)