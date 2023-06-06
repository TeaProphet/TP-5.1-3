package vsu.tp53.onboardapplication.home.service

import android.content.ContentValues
import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.entity.SessionBody
import vsu.tp53.onboardapplication.model.entity.SessionInfoBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val baseUrl = "http://193.233.49.112"
class SessionService    (
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

    init {
        val objectMapper = ObjectMapper()
        val javaTimeModule = JavaTimeModule()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        javaTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
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

                sessionList.body!!.toList()
            }
        }

    suspend fun getSessionInfo(sessionId : Int): SessionInfoBody? {
        return withContext(Dispatchers.IO) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

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

}