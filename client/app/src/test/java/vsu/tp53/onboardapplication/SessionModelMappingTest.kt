package vsu.tp53.onboardapplication

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import vsu.tp53.onboardapplication.model.entity.PlayerBody
import vsu.tp53.onboardapplication.model.entity.SessionBody
import vsu.tp53.onboardapplication.model.entity.SessionInfoBody
import java.time.LocalDateTime

class SessionModelMappingTest {
    @Test
    fun sessionBodyTestJsonToDomain() {
        val json = """
            {
                "name":"Игры",
                "city_address":"Воронеж, Московский проспект, 114",
                "date_time":"2023.05.23 16:00",
                "players_max":15
            }
        """.trimIndent()

        val expected = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )

        val actual = Json.decodeFromString<SessionBody>(json)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun sessionBodyTestDomainToJson() {
        val sessionBody = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )

        val expected =
            "{\"name\":\"Игры\",\"city_address\":\"Воронеж, Московский проспект, 114\",\"date_time\":\"2023.05.23 16:00\",\"players_max\":15}"

        val actual = Json.encodeToString(sessionBody)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun sessionTestJsonToDomain() {
        val json = """
            {
                "1":
                {
                    "name":"Игры",
                    "city_address":"Воронеж, Московский проспект, 114",
                    "date_time":"2023.05.23 16:00",
                    "players_max":15
                }
            }
        """.trimIndent()

        val expected = HashMap<Int, SessionBody>()
        val sessionBody = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )
        expected[1] = sessionBody

        val actual = Json.decodeFromString<HashMap<Int, SessionBody>>(json)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun sessionTestDomainToJson() {
        val sessionBody = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )
        val session = HashMap<Int, SessionBody>()
        session[1] = sessionBody

        val expected =
            "{\"1\":{\"name\":\"Игры\",\"city_address\":\"Воронеж, Московский проспект, 114\",\"date_time\":\"2023.05.23 16:00\",\"players_max\":15}}"

        val actual = Json.encodeToString(session)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun sessionsTestJsonToDomain() {
        val json = """
            {
                "1":
                {
                    "name":"Игры",
                    "city_address":"Воронеж, Московский проспект, 114",
                    "date_time":"2023.05.23 16:00",
                    "players_max":15
                },
                "22":
                {
                    "name":"Покер",
                    "city_address":"Воронеж, Плехановская, 14",
                    "date_time":"2022.12.23 17:55",
                    "players_max":20
                }
            }
        """.trimIndent()

        val expected = HashMap<Int, SessionBody>()
        val sessionBody1 = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )
        val sessionBody2 = SessionBody(
            "Покер",
            "Воронеж, Плехановская, 14",
            LocalDateTime.of(2022, 12, 23, 17, 55),
            20
        )
        expected[1] = sessionBody1
        expected[22] = sessionBody2

        val actual = Json.decodeFromString<HashMap<Int, SessionBody>>(json)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun sessionsTestDomainToJson() {
        val map = HashMap<Int, SessionBody>()
        val sessionBody1 = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )
        val sessionBody2 = SessionBody(
            "Покер",
            "Воронеж, Плехановская, 14",
            LocalDateTime.of(2022, 12, 23, 17, 55),
            20
        )
        map[1] = sessionBody1
        map[22] = sessionBody2

        val expected =
            "{\"1\":{\"name\":\"Игры\",\"city_address\":\"Воронеж, Московский проспект, 114\",\"date_time\":\"2023.05.23 16:00\",\"players_max\":15},\"22\":{\"name\":\"Покер\",\"city_address\":\"Воронеж, Плехановская, 14\",\"date_time\":\"2022.12.23 17:55\",\"players_max\":20}}"

        val actual = Json.encodeToString(map)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun sessionInfoTestJsonToDomain() {
        val json = """
            {
                "1":
                {
                    "name":"Игры",
                    "city_address":"Воронеж, Московский проспект, 114",
                    "date_time":"2023.05.23 16:00",
                    "players_max":15,
                    "players":
                    {
                        "Lola":
                            {
                            "reputation": 10       
                            }         
                    }
                }
            }
        """.trimIndent()

        val expected = HashMap<Int, SessionInfoBody>()

        val player = PlayerBody(10.0)
        val playerMap = HashMap<String, PlayerBody>()
        playerMap["Lola"] = player
        val sessionInfoBody =
            SessionInfoBody("Игры", "Воронеж, Московский проспект, 114", LocalDateTime.of(2023, 5, 23, 16, 0), 15, playerMap)
        expected[1] = sessionInfoBody

        val actual = Json.decodeFromString<HashMap<Int, SessionInfoBody>>(json)

        Assert.assertEquals(expected, actual)
    }
}