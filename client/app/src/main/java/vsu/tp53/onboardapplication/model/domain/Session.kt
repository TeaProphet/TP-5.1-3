package vsu.tp53.onboardapplication.model.domain

import kotlinx.serialization.Serializable
import vsu.tp53.onboardapplication.model.entity.LocalDateTimeSerializer
import vsu.tp53.onboardapplication.model.entity.PlayerBody
import vsu.tp53.onboardapplication.model.entity.SessionBody
import vsu.tp53.onboardapplication.model.entity.SessionInfoBody
import java.time.LocalDateTime

@Serializable
data class Session(
    var sessionId: Int,
    var name: String,
    var city_address: String,
    @Serializable(LocalDateTimeSerializer::class)
    var date_time: LocalDateTime,
    var players_max: Int
) {
    fun mapToEntity() = SessionBody(sessionId, name, city_address, date_time, players_max)
}

@Serializable
data class SessionId(
    var sessionId: Int
)

@Serializable
data class SessionInfo(
    var sessionId: Int,
    var name: String,
    var owner: String,
    var city_address: String,
    var games: String,
    @Serializable(LocalDateTimeSerializer::class)
    var date_time: LocalDateTime,
    var players_max: Int,
    var players: Array<String>
) {
    fun mapToEntity() = SessionInfoBody(name, city_address, owner, games, date_time, players_max, players)
}