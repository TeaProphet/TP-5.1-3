package vsu.tp53.onboardapplication.auth.service

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.entity.ChangeProfile
import vsu.tp53.onboardapplication.model.entity.ChangeProfileEntity
import vsu.tp53.onboardapplication.model.entity.ChangeReputationEntity
import vsu.tp53.onboardapplication.model.entity.ErrorEntity
import vsu.tp53.onboardapplication.model.entity.ProfileBanEntity
import vsu.tp53.onboardapplication.model.entity.ProfileBanEntityResponse
import vsu.tp53.onboardapplication.model.entity.ProfileInfoEntity
import vsu.tp53.onboardapplication.model.entity.ReputationEntity
import vsu.tp53.onboardapplication.model.entity.SearchProfile
import vsu.tp53.onboardapplication.sqlitedb.UserTokenDbHelper

class ProfileService(
    private val restTemplate: RestTemplate,
    val context: Context
) {
    private val getProfileUrl: String = "http://193.233.49.112/get_profile_info/{nickname}"
    private val editProfileUrl: String = "http://193.233.49.112/change_profile/{idToken}"
    private val increaseRepUrl: String = "http://193.233.49.112/plus_reputation/"
    private val decreaseRepUrl: String = "http://193.233.49.112/minus_reputation/"
    private val banUserUrl: String = "http://193.233.49.112/ban/"
    private val unbanUserUrl: String = "http://193.233.49.112/unban/"
    private val dbHelper = UserTokenDbHelper(context)
    private val authService = AuthService(restTemplate, context)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    suspend fun getProfileInfo(login: String = ""): ProfileInfoEntity? {

        return withContext(Dispatchers.IO) {
            Log.i("Profile-last", login)

            if (login != "") {
                return@withContext null
            }

            val lastNickname = prefs.getString(LAST_NICKNAME_KEY, "")
            Log.i("Profile-lasLogin", lastNickname!!)

            var params: MutableMap<String, String> = HashMap()
            params["nickname"] = lastNickname

            val searchedProfile = SearchProfile(lastNickname)
            Log.i("Profile-searchLogin", searchedProfile.toString())
            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())
            restTemplate.messageConverters.add(GsonHttpMessageConverter())

            val profResp = restTemplate.getForObject(
                "http://193.233.49.112/get_profile_info/$lastNickname",
                ProfileInfoEntity::class.java,
            )

            Log.i("ProfileServ", profResp.toString())

            val profileInfo = restTemplate.getForObject(
                getProfileUrl,
                ProfileInfoEntity::class.java,
                params
            )

            Log.i("ProfileService", profileInfo.error.toString())

            Log.i("Profile-info", profileInfo.toString())

            profileInfo
        }
    }

    suspend fun changeProfile(userInfo: ChangeProfile) {
        withContext(Dispatchers.IO) {
            val lastLogin = prefs.getString(LAST_LOGIN_KEY, "")!!
            val token = authService.getRowByLogin(lastLogin)?.tokenId
            val changeProfileEntity = ChangeProfileEntity(
                token!!,
                userInfo.age,
                userInfo.games,
                userInfo.vk,
                userInfo.tg
            )

            val entity: HttpEntity<ChangeProfileEntity> = HttpEntity(changeProfileEntity)

            val params: MutableMap<String, String> = HashMap()
            params["idToken"] = token

            Log.i("Profile-info", "Change profile to $userInfo")

            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            val profileInfoError: ResponseEntity<ErrorEntity>? = restTemplate.exchange(
                editProfileUrl,
                HttpMethod.PUT,
                entity,
                ErrorEntity::class.java,
                params
            )
        }
    }

    suspend fun increaseReputation(changeRep: ChangeReputationEntity): ReputationEntity? {
        return withContext(Dispatchers.IO) {
            Log.i("Profile-repInc", changeRep.toString())
            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())

            restTemplate.postForObject(
                increaseRepUrl,
                changeRep,
                ReputationEntity::class.java
            )
        }
    }

    suspend fun decreaseReputation(changeRep: ChangeReputationEntity): ReputationEntity? {
        return withContext(Dispatchers.IO) {
            Log.i("Profile-repDec", changeRep.toString())
            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())

            restTemplate.postForObject(
                decreaseRepUrl,
                changeRep,
                ReputationEntity::class.java
            )
        }
    }

    suspend fun banUser(banUser: ProfileBanEntity): ProfileBanEntityResponse {
        return withContext(Dispatchers.IO) {
            Log.i("ProfileService-BanBef",  banUser.toString())
            banUser.idToken = getUserToken()
            Log.i("ProfileService-BanAft",  banUser.toString())
            restTemplate.postForObject(
                banUserUrl,
                banUser,
                ProfileBanEntityResponse::class.java
            )
        }
    }

    suspend fun unbanUser(unbanUser: ProfileBanEntity): ProfileBanEntityResponse {
        return withContext(Dispatchers.IO) {
            unbanUser.idToken = getUserToken()
            restTemplate.postForObject(
                unbanUserUrl,
                unbanUser,
                ProfileBanEntityResponse::class.java
            )
        }
    }

    fun getUserLogin(): String {
        return prefs.getString(LAST_LOGIN_KEY, "").toString()
    }

    fun getUserNickname(): String {
        return prefs.getString(LAST_NICKNAME_KEY, "").toString()
    }

    private fun getUserToken(): String {
        return authService.getRowByLogin(getUserLogin())?.tokenId.toString()
    }
}