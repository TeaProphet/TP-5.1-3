package vsu.tp53.onboardapplication.model.domain

import kotlinx.serialization.Serializable
import vsu.tp53.onboardapplication.model.entity.UserAuthorize
import vsu.tp53.onboardapplication.model.entity.UserRegisterPost
import vsu.tp53.onboardapplication.model.entity.UserTokenPost
import java.time.LocalDateTime

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