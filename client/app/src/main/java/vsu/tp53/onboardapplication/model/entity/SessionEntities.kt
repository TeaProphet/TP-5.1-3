package vsu.tp53.onboardapplication.model.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import vsu.tp53.onboardapplication.model.domain.Session
import vsu.tp53.onboardapplication.model.domain.SessionInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class SessionBody(
    var name: String,
    var city_address: String,
    @Serializable(LocalDateTimeSerializer::class)
    var date_time: LocalDateTime,
    var players_max: Int
) {
    fun mapToDomain(id: Int) = Session(id, name, city_address, date_time, players_max)
}

@Serializable
data class SessionIdPost(
    var sessionId: Int
)

@Serializable
data class SessionInfoBody(
    var name: String,
    var city_address: String,
    @Serializable(LocalDateTimeSerializer::class)
    var date_time: LocalDateTime,
    var players_max: Int,
    var players: Map<String, PlayerBody>
) {
    fun mapToDomain(id: Int) = SessionInfo(id, name, city_address, date_time, players_max, players)
}

@Serializable
data class PlayerBody(
    var reputation: Double
)

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val df = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), df)
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(df).toString())
    }
}