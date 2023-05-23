package vsu.tp53.onboardapplication.auth.service

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import androidx.preference.PreferenceManager
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.entity.ChangeProfile
import vsu.tp53.onboardapplication.model.entity.ChangeProfileEntity
import vsu.tp53.onboardapplication.model.entity.ErrorEntity
import vsu.tp53.onboardapplication.model.entity.ProfileInfoEntity
import vsu.tp53.onboardapplication.model.entity.SearchProfile
import vsu.tp53.onboardapplication.sqlitedb.UserTokenDbHelper

class ProfileService(
    private val restTemplate: RestTemplate,
    val context: Context
) {
    private val getProfileUrl: String = "http://192.168.0.101:8000/get_profile_info/"
    private val editProfileUrl: String = "http://192.168.0.101:8000/change_profile/"
    private val dbHelper = UserTokenDbHelper(context)
    private val authService = AuthService(restTemplate, context)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getProfileInfo(login: String = ""): ProfileInfoEntity? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (login != "") {
            return null
        }

        val lastLogin = prefs.getString(LAST_LOGIN, "")
        val searchedProfile = SearchProfile(lastLogin!!)
        restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
        val profileInfo: ProfileInfoEntity = restTemplate.postForObject(
            getProfileUrl,
            searchedProfile,
            ProfileInfoEntity::class.java
        )!!

        Log.i("Profile-info", profileInfo.toString())

        return profileInfo
    }

    fun changeProfile(userInfo: ChangeProfile) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val lastLogin = prefs.getString(LAST_LOGIN, "")!!
        val token = authService.getRowByLogin(lastLogin)?.tokenId
        val changeProfileEntity = ChangeProfileEntity(
            token!!,
            userInfo.age,
            userInfo.games,
            userInfo.vk,
            userInfo.tg
        )
        Log.i("Profile-info", "Change profile to $userInfo" )
        restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
        val profileInfoError: ErrorEntity? = restTemplate.postForObject(
            editProfileUrl,
            changeProfileEntity,
            ErrorEntity::class.java
        )
    }

    fun getUserLogin(): String {
        return prefs.getString(LAST_LOGIN, "").toString()
    }
}