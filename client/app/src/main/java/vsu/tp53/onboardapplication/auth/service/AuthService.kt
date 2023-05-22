package vsu.tp53.onboardapplication.auth.service

import android.os.StrictMode
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.domain.User
import vsu.tp53.onboardapplication.model.entity.UserRegAuthorizePost
import vsu.tp53.onboardapplication.model.entity.UserToken


class AuthService(
    private val restTemplate: RestTemplate
) {
    private val regUrl: String = "http://192.168.0.103:8000/register/"

    fun registerUser(user: User) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val userRegAuthorizePost: UserRegAuthorizePost = user.mapToEntity()
        Log.i("AuthServ-regObjPost", userRegAuthorizePost.toString())

        restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
        val userToken = restTemplate.postForObject(
            regUrl,
            userRegAuthorizePost,
            UserToken::class.java
        )!!

        Log.i("Auth-regSuc", userToken.toString())

//        val dataBaseHandler:DatabaseH
    }
}