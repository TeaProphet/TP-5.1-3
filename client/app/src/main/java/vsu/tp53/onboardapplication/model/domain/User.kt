package vsu.tp53.onboardapplication.model.domain

import kotlinx.serialization.Serializable
import vsu.tp53.onboardapplication.model.entity.LocalDateTimeSerializer
import vsu.tp53.onboardapplication.model.entity.UserRegAuthorizePost
import vsu.tp53.onboardapplication.model.entity.UserTokenPost
import java.time.LocalDateTime

@Serializable
data class User(
    var login: String,
    var password: String
) {
    fun mapToEntity() = UserRegAuthorizePost(login, password)
}

@Serializable
data class Token(
    var idToken: String
) {
    fun mapToEntity() = UserTokenPost(idToken)
}

data class UserLogInInfo(
    val login: String,
    val tokenId: String,
    val expire: LocalDateTime
)