package vsu.tp53.onboardapplication

import com.google.gson.Gson
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.entity.ProfileInfoEntity
import vsu.tp53.onboardapplication.model.entity.SessionBody
import java.time.LocalDateTime

class ProfileModelMappingTest {
    private val getProfileUrl: String = "http://193.233.49.112/get_profile_info/{nickname}"

    @Test
    fun getProfileInfoMapTest() {
        val json = """
            {
              "error": "INVALID", 
              "age": 19,
              "games": "Игры, карты",
              "vk": "https://vk.com/id289765628",
              "tg": "@Lololo",
              "reputation": 1.0,
              "played_sessions": 
                [1]
            }
        """.trimIndent()

        val sessionBody = SessionBody(
            "Игры",
            "Воронеж, Московский проспект, 114",
            LocalDateTime.of(2023, 5, 23, 16, 0),
            15
        )
        val map = mutableListOf(1)
        val expected =
            ProfileInfoEntity(
                "INVALID",
                19,
                "Игры, карты",
                "https://vk.com/id289765628",
                "@Lololo",
                1.0,
                map,
                is_admin = false,
                is_banned = false
            )

        val actual = Json.decodeFromString<ProfileInfoEntity>(json)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun getProfileInfoTest() {
        val restTemplate = RestTemplate()
        restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())

        val params: MutableMap<String, String> = HashMap()
        params["nickname"] = "user1"

//        val profResp = restTemplate.getForObject(
//            getProfileUrl,
//            ProfileInfoEntity::class.java,
//            params
//        )

        val profResp1 = restTemplate.exchange(
            getProfileUrl,
            HttpMethod.GET,
            null,
            ProfileInfoEntity::class.java,
            params
        )

        print(profResp1.toString())
    }

    @Test
    fun mapProfileInfoTest() {
        val json = """
            {
              "error": "",
              "age": 25,
              "games": "Minecraft",
              "vk": "https://vk.com/user123",
              "tg": "@user123",
              "reputation": 4.5,
              "played_sessions": [1, 2, 3],
              "is_admin": false,
              "is_banned": false
            }
        """.trimIndent()

        val actual = Json.decodeFromString<ProfileInfoEntity>(json)

        print(actual)
    }

    @Test
    fun getProfileInfoGsonTest() {
        val nickname = "user1"
        val url = "http://193.233.49.112/get_profile_info/$nickname"
        val restTemplate = RestTemplate()
        restTemplate.messageConverters.add(GsonHttpMessageConverter())

        val response = restTemplate.getForObject(url, String::class.java)
        val gson = Gson()
        val profResp = gson.fromJson(response, ProfileInfoEntity::class.java)
        print(profResp)
    }
}