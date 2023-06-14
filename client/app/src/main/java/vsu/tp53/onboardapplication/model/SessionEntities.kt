package vsu.tp53.onboardapplication.model

import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class SessionBody(
    var sessionId: Int,
    var name: String,
    var city_address: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    var date_time: LocalDateTime,
    var players_max: Int
) {

    constructor() : this(0, "", "", LocalDateTime.now(), 0)
}

@Serializable
data class SessionInfoBody(
    var name: String,
    var city_address: String,
    var owner: String,
    var games: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    var date_time: LocalDateTime,
    var players_max: Int,
    var players: Array<String>
) {
    fun mapToDomain(id: Int) = Session(id, name, city_address, date_time, players_max)

    constructor() : this("", "", "", "", LocalDateTime.now(), 0, emptyArray())
}

@Serializable
data class SessionEntity(
    var idToken: String?,
    var name: String?,
    var city_address: String?,
    var games: String?,
    var date_time: String?,
    var players: Array<String>?,
    var players_max: Int?
) {
    constructor() : this("", "", "", "", LocalDateTime.now().toString(), emptyArray(), 0)
}

@Serializable
data class PlayerBody(
    var reputation: Double
)

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