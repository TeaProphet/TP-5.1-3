package vsu.tp53.onboardapplication.service

import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.ErrorEntity
import vsu.tp53.onboardapplication.model.SessionBody
import vsu.tp53.onboardapplication.model.SessionEntity
import vsu.tp53.onboardapplication.model.SessionInfoBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val baseUrl = "http://193.233.18.159"

class SessionService(
    private val restTemplate: RestTemplate,
    val context: Context
) {
    private val sessionsUrl: String = "/get_sessions/"
    private val sessionInfoUrl: String = "/get_session_info/"
    private val createSessionUrl: String = "/create_session/"
    private val deleteSessionUrl: String = "/delete_session/"
    private val changeSessionUrl: String = "/change_session_name/"
    private val joinSessionUrl: String = "/join_session/"
    private val leaveSessionUrl: String = "/leave_session/"
    private val searchSessionUrl: String = "/search_by_id/"
    private val authService: AuthService = AuthService(restTemplate, context)
    private val profileService: ProfileService = ProfileService(restTemplate, context)

    init {
        val objectMapper = ObjectMapper()
        val javaTimeModule = JavaTimeModule()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        javaTimeModule.addDeserializer(
            LocalDateTime::class.java,
            LocalDateTimeDeserializer(dateTimeFormatter)
        )
        objectMapper.registerModule(javaTimeModule)

        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = objectMapper
        restTemplate.messageConverters.add(converter)
    }

    suspend fun getSessions(): List<SessionBody> {
        return withContext(Dispatchers.IO) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            val url = baseUrl + sessionsUrl;
            val sessionList: ResponseEntity<Array<SessionBody>> = restTemplate.getForEntity(
                url,
                //тип должен быть сущностью, с json properties, чтобы корректно парситься, насколько я поняла
                Array<SessionBody>::class.java
            )
            Log.i("SessionService", sessionList.body!!.toList().toString())
            sessionList.body!!.toList()
        }
    }

    suspend fun getSessionInfo(sessionId: Int): SessionInfoBody? {
        return withContext(Dispatchers.IO) {
            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
            restTemplate.messageConverters.add(GsonHttpMessageConverter())

            val url = "$baseUrl$sessionInfoUrl$sessionId";
            val sessionInfo = restTemplate.getForObject(
                url,
                //то же самое с типом
                SessionInfoBody::class.java
            )

            sessionInfo
        }
    }

    suspend fun createSession(sessionEntity: SessionEntity): ErrorEntity? {
        return withContext(Dispatchers.IO) {
            Log.i("SessionService", "Create")
            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
            restTemplate.messageConverters.add(GsonHttpMessageConverter())

            val url = "$baseUrl$createSessionUrl"

            Log.i("SessionService", "url: $url")

            sessionEntity.idToken = profileService.getUserToken()

            Log.i("SessionService", sessionEntity.toString())
            val resp = restTemplate.postForEntity(
                url,
                sessionEntity,
                ErrorEntity::class.java
            )
            Log.i("SessionSeervice-create", resp.toString())

            resp.body
        }
    }

    suspend fun deleteSession(sessionId: Int) {
        withContext(Dispatchers.IO) {
            Log.i("SessionService", "Delete")

            val url = "$baseUrl$deleteSessionUrl$sessionId?idToken={idToken}"
            val params = HashMap<String, String>()
            params["idToken"] = profileService.getUserToken()

            val resp = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Void::class.java,
                params
            )
            Log.i("SessionService", "Delete session resp; " + resp.statusCode.toString())
        }
    }

    suspend fun changeSessionName(newName: String, oldName: String, sessionId: Int) {
        withContext(Dispatchers.IO) {
            Log.i("SessionService", "Change name")
            Log.i("SessionService", "New name: $newName")

            val url = "$baseUrl${changeSessionUrl}s_id:{sessionId}name:{newName}?idToken={idToken}"
            val params = HashMap<String, String>()
            params["sessionId"] = "$sessionId"
            params["newName"] = newName
            params["idToken"] = profileService.getUserToken()

            val resp = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                null,
                Void::class.java,
                params
            )
            Log.i("SessionService", "Change name resp; " + resp.statusCode.toString())
        }
    }

    suspend fun joinSession(sessionId: Int) {
        withContext(Dispatchers.IO) {
            Log.i("SessionService", "Join session")

            val url = "$baseUrl$joinSessionUrl$sessionId?idToken={idToken}"

            val params = HashMap<String, String>()
            params["idToken"] = profileService.getUserToken()

            val resp = restTemplate.postForEntity(
                url,
                null,
                Void::class.java,
                params
            )

            Log.i("SessionService", "Join session resp; " + resp.statusCode.toString())
        }
    }

    suspend fun leaveSession(sessionId: Int) {
        withContext(Dispatchers.IO) {
            Log.i("SessionService", "Leave session")

            val url = "$baseUrl$leaveSessionUrl$sessionId?idToken={idToken}"

            val params = HashMap<String, String>()
            params["idToken"] = profileService.getUserToken()

            val resp = restTemplate.postForEntity(
                url,
                null,
                Void::class.java,
                params
            )

            Log.i("SessionService", "Leave session resp; " + resp.statusCode.toString())
        }
    }

    suspend fun searchSession(sessionId: Int): List<SessionBody> {
        return withContext(Dispatchers.IO) {
            Log.i("SessionService", "Search session")

            val url = "$baseUrl$searchSessionUrl$sessionId?idToken={idToken}"

            val params = HashMap<String, String>()
            params["idToken"] = profileService.getUserToken()
//            val body = HashMap<String, Int>()
//            body["requested_id"] = sessionId

            val resp = restTemplate.getForObject(
                url,
                Array<SessionBody>::class.java,
                params
            )

            Log.i("SessionService", "Search session resp; $resp")
            resp.toList()
        }
    }
}