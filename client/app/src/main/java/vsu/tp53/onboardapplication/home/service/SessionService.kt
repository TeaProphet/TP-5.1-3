package vsu.tp53.onboardapplication.home.service

import android.content.Context
import android.os.StrictMode
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.entity.SessionBody

const val baseUrl = "http://193.233.49.112"
class SessionService (
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

    suspend fun getSessions(): Array<SessionBody> {
        return withContext(Dispatchers.IO) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
            restTemplate.messageConverters.add(GsonHttpMessageConverter())

            val url = baseUrl + sessionsUrl;
            val sessionList = restTemplate.getForObject(
                url,
                //тип должен быть сущностью, с json properties, чтобы корректно парситься, насколько я поняла
                Array<SessionBody>::class.java
            )

            sessionList
        }
    }

    suspend fun getSessionInfo(sessionId : Int): SessionBody? {
        return withContext(Dispatchers.IO) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
            restTemplate.messageConverters.add(GsonHttpMessageConverter())

            val url = "$baseUrl$sessionInfoUrl$sessionId";
            val sessionInfo: SessionBody? = null
            try {
                val sessionInfo = restTemplate.getForObject(
                    url,
                    //то же самое с типом
                    SessionBody::class.java
                )
            } catch (e: Error) {
                Log.i("Sessions", "65")
            }

            sessionInfo
        }
    }

}