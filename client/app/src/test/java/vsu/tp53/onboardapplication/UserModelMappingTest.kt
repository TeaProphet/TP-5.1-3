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
import vsu.tp53.onboardapplication.model.entity.UserRegAuthorize
import vsu.tp53.onboardapplication.model.entity.UserRegAuthorizePost
import vsu.tp53.onboardapplication.model.entity.UserToken
import vsu.tp53.onboardapplication.model.entity.UserTokenPost

@RunWith(MockitoJUnitRunner::class)
class UserModelMappingTest {

    @Mock
    lateinit var restTemplate: RestTemplate

    @Test
    fun userRegAuthorizeTestFromJsonToDomain() {
        val json = "{\"error\":\"INVALID\",\"login\":\"user\",\"password\":\"password\"}"
        val expected = UserRegAuthorize("INVALID", "user", "password")
        val actual = Json.decodeFromString<UserRegAuthorize>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userRegAuthorizeTestFromDomainToJson() {
        val userRegAuthorize = UserRegAuthorize("INVALID", "user", "password")
        val expected =
            "{\"error\":\"INVALID\",\"login\":\"user\",\"password\":\"password\"}"
        val actual = Json.encodeToString(userRegAuthorize)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userRegAuthorizePostTestFromJsonToDomain() {
        val json = "{\"login\":\"user\",\"password\":\"password\"}"
        val expected = UserRegAuthorizePost("user", "password")
        val actual = Json.decodeFromString<UserRegAuthorizePost>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userRegAuthorizePostTestFromDomainToJson() {
        val userRegAuthorizePost = UserRegAuthorizePost("user", "password")
        val expected = "{\"login\":\"user\",\"password\":\"password\"}"
        val actual = Json.encodeToString(userRegAuthorizePost)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestFromJsonToDomain() {
        val json = "{\"error\":\"INVALID\",\"idToken\":\"token12\"}"
        val expected = UserToken("INVALID", "token12")
        val actual = Json.decodeFromString<UserToken>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestFromJsonToDomainObjectMapper() {
        val mapper = ObjectMapper()
        val json = "{\"error\":\"INVALID\",\"idToken\":\"token12\"}"
        val expected = UserToken("INVALID", "token12")
        val actual = mapper.reader(UserToken::class.java).readValue<UserToken>(json)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestFromDomainToJson() {
        val userToken = UserToken("INVALID", "token12")
        val expected = "{\"error\":\"INVALID\",\"idToken\":\"token12\"}"
        val actual = Json.encodeToString(userToken)
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
        val userRegAuthorize = UserRegAuthorize("INVALID", "user", "password")
        val expected = User("user", "password")
        val actual = userRegAuthorize.mapToDomain()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTokenTestMapper() {
        val userToken = UserToken("INVALID", "token12")
        val expected = Token("token12")
        val actual = userToken.mapToDomain()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun userTestMapper() {
        val user = User("user", "password")
        val expected = UserRegAuthorizePost("user", "password")
        val actual = user.mapToEntity()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun tokenTestMapper() {
        val token = Token("token12")
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
        val actual = Json.decodeFromString<UserToken>(response)

        val expected = UserToken("INVALID", "token12")
        Assert.assertEquals(expected, actual)
    }
}