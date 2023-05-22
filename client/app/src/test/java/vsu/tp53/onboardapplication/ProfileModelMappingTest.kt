package vsu.tp53.onboardapplication

import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import vsu.tp53.onboardapplication.model.entity.ProfileInfoEntity
import vsu.tp53.onboardapplication.model.entity.SessionBody
import java.time.LocalDateTime

class ProfileModelMappingTest {
    @Test
    fun getProfileInfoTest() {
        val json = """
            {
              "age": 19,
              "games": "Игры, карты",
              "vk": "https://vk.com/id289765628",
              "tg": "@Lololo",
              "played_sessions": {
                "1":              
                {
                  "name":"Игры",
                  "city_address":"Воронеж, Московский проспект, 114",
                  "date_time":"2023.05.23 16:00",
                  "players_max":15
                }
              }
            }
        """.trimIndent()

        val sessionBody = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )
        val map = HashMap<Int, SessionBody>()
        map[1] = sessionBody
        val expected =
            ProfileInfoEntity(19, "Игры, карты", "https://vk.com/id289765628", "@Lololo", map)

        val actual = Json.decodeFromString<ProfileInfoEntity>(json)

        Assert.assertEquals(expected, actual)
    }
}