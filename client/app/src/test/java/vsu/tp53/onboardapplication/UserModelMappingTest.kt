package vsu.tp53.onboardapplication

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.domain.Token
import vsu.tp53.onboardapplication.model.domain.User
import vsu.tp53.onboardapplication.model.entity.UserRegisterResponse
import vsu.tp53.onboardapplication.model.entity.UserRegisterPost
import vsu.tp53.onboardapplication.model.entity.UserTokenResponse
import vsu.tp53.onboardapplication.model.entity.UserTokenPost

@RunWith(MockitoJUnitRunner::class)
class UserModelMappingTest {

    @Mock
    lateinit var restTemplate: RestTemplate

    @Test
    fun userRegAuthorizeTestFromJsonToDomain() {
        val json = "{\"error\":\"INVALID\",\"login\":\"user\",\"password\":\"password\"}"
        val expected = UserRegisterResponse("INVALID", "user", "password")
        val actual = Json.decodeFromString<UserRegisterResponse>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userRegAuthorizeTestFromDomainToJson() {
        val userRegisterResponse = UserRegisterResponse("INVALID", "user", "password")
        val expected =
            "{\"error\":\"INVALID\",\"login\":\"user\",\"password\":\"password\"}"
        val actual = Json.encodeToString(userRegisterResponse)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userRegAuthorizePostTestFromJsonToDomain() {
        val json = "{\"login\":\"user\",\"password\":\"password\"}"
        val expected = UserRegisterPost("user", "password")
        val actual = Json.decodeFromString<UserRegisterPost>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userRegAuthorizePostTestFromDomainToJson() {
        val userRegisterPost = UserRegisterPost("user", "password")
        val expected = "{\"login\":\"user\",\"password\":\"password\"}"
        val actual = Json.encodeToString(userRegisterPost)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestFromJsonToDomain() {
        val json = "{\"error\":\"INVALID\",\"idToken\":\"token12\"}"
        val expected = UserTokenResponse("INVALID", "token12")
        val actual = Json.decodeFromString<UserTokenResponse>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestFromJsonToDomainObjectMapper() {
        val mapper = ObjectMapper()
        val json = "{\"error\":\"INVALID\",\"idToken\":\"token12\"}"
        val expected = UserTokenResponse("INVALID", "token12")
        val actual = mapper.reader(UserTokenResponse::class.java).readValue<UserTokenResponse>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestFromDomainToJson() {
        val userTokenResponse = UserTokenResponse("INVALID", "token12")
        val expected = "{\"error\":\"INVALID\",\"idToken\":\"token12\"}"
        val actual = Json.encodeToString(userTokenResponse)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestPostFromJsonToDomain() {
        val json = "{\"idToken\":\"token12\"}"
        val expected = UserTokenPost("token12")
        val actual = Json.decodeFromString<UserTokenPost>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestPostFromDomainToJson() {
        val userTokenPost = UserTokenPost("token12")
        val expected = "{\"idToken\":\"token12\"}"
        val actual = Json.encodeToString(userTokenPost)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userRegAuthorizeTestMapper() {
        val userRegisterResponse = UserRegisterResponse("INVALID", "nickname", "login", "password")
        val expected = User("INVALID", "nickname", "login", "password")
        val actual = userRegisterResponse.mapToDomain()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestMapper() {
        val userTokenResponse = UserTokenResponse("INVALID", "token12")
        val expected = Token("INVALID", "token12")
        val actual = userTokenResponse.mapToDomain()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTestMapper() {
        val user = User("INVALID", "nickname", "login", "password")
        val expected = UserRegisterPost("user", "password")
        val actual = user.mapToUserRegEntity()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun tokenTestMapper() {
        val token = Token("INVALID", "token12")
        val expected = UserTokenPost("token12")
        val actual = token.mapToEntity()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun tokenTestRestTemplate() {
        Mockito.`when`(
            restTemplate.getForObject(
                "http://127.0.0.1:8000/register",
                String::class.java
            )
        ).thenReturn("{\"error\":\"INVALID\",\"idToken\":\"token12\"}")

        val response: String = restTemplate.getForObject(
            "http://127.0.0.1:8000/register",
            String::class.java
        ) as String
        val actual = Json.decodeFromString<UserTokenResponse>(response)

        val expected = UserTokenResponse("INVALID", "token12")
        Assert.assertEquals(expected, actual)
    }
}