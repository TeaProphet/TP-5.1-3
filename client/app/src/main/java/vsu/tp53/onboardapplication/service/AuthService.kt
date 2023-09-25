package vsu.tp53.onboardapplication.service

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.provider.BaseColumns
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.Token
import vsu.tp53.onboardapplication.model.User
import vsu.tp53.onboardapplication.model.UserAuthorize
import vsu.tp53.onboardapplication.model.UserLogInInfo
import vsu.tp53.onboardapplication.model.UserRegisterPost
import vsu.tp53.onboardapplication.model.UserRegisterResponse
import vsu.tp53.onboardapplication.model.UserTokenPost
import vsu.tp53.onboardapplication.model.UserTokenResponse
import vsu.tp53.onboardapplication.sqlitedb.UserTokenContract
import vsu.tp53.onboardapplication.sqlitedb.UserTokenDbHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


const val IS_LOGIN_KEY = "IsLogin"
const val LAST_LOGIN_KEY = "LastLogin"
const val LAST_NICKNAME_KEY = "LastNickname"

class AuthService(
    private val restTemplate: RestTemplate,
    val context: Context
) {
    private val regUrl: String = "http://193.233.18.159/register/"
    private val authUrl: String = "http://193.233.18.159/authorize/"
    private val tokenAuthUrl: String = "http://193.233.18.159/token_authorize/"
    private val dbHelper = UserTokenDbHelper(context)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    suspend fun registerUser(user: User): Token {
//        dropTable()
        try {
            return withContext(Dispatchers.IO) {
                val userRegisterPost: UserRegisterPost = user.mapToUserRegEntity()
                Log.i("AuthServ-regObjPost", userRegisterPost.toString())

                restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())

                val userTokenResponse =
                    restTemplate.postForObject(
                        regUrl,
                        userRegisterPost,
                        UserTokenResponse::class.java
                    )!!

                Log.i("AuthServ", "Before check if error is null")
                if (userTokenResponse.error == null) {
                    Log.i("Auth-regSuc", userTokenResponse.toString())

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val db = dbHelper.writableDatabase
                    val values = ContentValues().apply {
                        put(UserTokenContract.UserTokenEntry.COLUMN_NICKNAME, user.nickname)
                        put(UserTokenContract.UserTokenEntry.COLUMN_LOGIN, user.login)
                        put(
                            UserTokenContract.UserTokenEntry.COLUMN_TOKEN,
                            userTokenResponse.idToken
                        )
                        put(
                            UserTokenContract.UserTokenEntry.COLUMN_EXPIRE,
                            formatter.format(LocalDateTime.now()).toString()
                        )
                    }
                    val newRowId =
                        db?.insert(
                            UserTokenContract.UserTokenEntry.TABLE_NAME,
                            null,
                            values
                        )

                    Log.i("Auth-newRowId", newRowId.toString())

                    prefs.edit().putBoolean(IS_LOGIN_KEY, true).apply()
                    prefs.edit().putString(LAST_LOGIN_KEY, user.login).apply()
                    prefs.edit().putString(LAST_NICKNAME_KEY, user.nickname).apply()
                    Log.i(
                        "Auth-ServReg",
                        "Last login is \"${prefs.getString(LAST_LOGIN_KEY, "")}\""
                    )
                    Log.i(
                        "Auth-ServReg",
                        "Last nickname is \"${prefs.getString(LAST_NICKNAME_KEY, "")}\""
                    )

                    readData()

                }
                userTokenResponse.mapToDomain()
            }
        } catch (e: Exception) {
            return Token(e.message, "", "")
        }
    }

    suspend fun authorizeUser(user: User): Token {
        try {
            return withContext(Dispatchers.IO) {
                Log.i("Auth-authBefore", user.toString())
                Log.i("Auth-authBeforeAfter", user.toString())
                val userAuthorizePost: UserAuthorize = user.mapToUserAuthEntity()
                Log.i("AuthServ-authObj", userAuthorizePost.toString())

                restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())

                val userTokenResponse =
                    restTemplate.postForObject(
                        authUrl,
                        userAuthorizePost,
                        UserTokenResponse::class.java
                    )!!

                Log.i("Auth-authSuc", userTokenResponse.toString())
                Log.i("Auth-authSuc", user.login)

                readData()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val db = dbHelper.writableDatabase
                val row = getRowByLogin(user.login)
                val values = ContentValues().apply {
                    put(UserTokenContract.UserTokenEntry.COLUMN_LOGIN, user.login)
                    put(UserTokenContract.UserTokenEntry.COLUMN_TOKEN, userTokenResponse.idToken)
                    put(
                        UserTokenContract.UserTokenEntry.COLUMN_EXPIRE,
                        formatter.format(LocalDateTime.now()).toString()
                    )
                    put(
                        UserTokenContract.UserTokenEntry.COLUMN_NICKNAME,
                        userTokenResponse.nickname
                    )
                }

                val selection = "${UserTokenContract.UserTokenEntry.COLUMN_LOGIN} LIKE ?"
                val selectionArgs = arrayOf(user.login)

                var newRowId = 0
                if (row != null) {
                    newRowId =
                        db?.update(
                            UserTokenContract.UserTokenEntry.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs
                        )!!
                }

                if (newRowId == 0) {
                    newRowId =
                        db?.insert(
                            UserTokenContract.UserTokenEntry.TABLE_NAME,
                            null,
                            values
                        )!!.toInt()
                }

                Log.i("Auth-updatedRowId", newRowId.toString())

                prefs.edit().putBoolean(IS_LOGIN_KEY, true).apply()
                prefs.edit().putString(LAST_LOGIN_KEY, user.login).apply()
                prefs.edit().putString(LAST_NICKNAME_KEY, userTokenResponse.nickname).apply()
                Log.i("Auth-ServAuth", "nickname to insert is ${user.nickname}")
                Log.i("Auth-ServAuth", "Last login is \"${prefs.getString(LAST_LOGIN_KEY, "")}\"")
                Log.i(
                    "Auth-ServAuth",
                    "Last login is logged? \"${prefs.getBoolean(IS_LOGIN_KEY, false)}\""
                )
                Log.i(
                    "Auth-ServAuth",
                    "Last nickname is  \"${prefs.getString(LAST_NICKNAME_KEY, "")}\""
                )

                userTokenResponse.mapToDomain()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun authToken() {
        val lastUserToken: String? = getRowByLogin(prefs.getString(LAST_LOGIN_KEY, "")!!)?.tokenId
        if (lastUserToken != null) {
            val userTokenPost = UserTokenPost(lastUserToken)
            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            val userRegAuth = restTemplate.postForObject(
                tokenAuthUrl,
                userTokenPost,
                UserRegisterResponse::class.java
            )!!

            //TODO: finish function
        }
    }

    fun checkTokenIsNotExpired(): Boolean {
        val lastUser: UserLogInInfo? = getRowByLogin(prefs.getString(LAST_LOGIN_KEY, "")!!)
        Log.i("AuthServ", "\"$lastUser\" from checkIfTokenExp")
        return if (lastUser == null) {
            Log.i("AuthServ", "user is null from checkIfTokenExp")
            prefs.edit().putBoolean(IS_LOGIN_KEY, false).apply()
            false
        } else {
            val expire = lastUser.expire
            Log.i("AuthServ", "$expire from checkIfTokenExp")
            val now = LocalDateTime.now()
            if (now.isAfter(expire)) {
                prefs.edit().putBoolean(IS_LOGIN_KEY, false).apply()
            }
            !now.isAfter(expire)
        }
    }

    fun checkIfUserLoggedIn(): Boolean {
        Log.i(
            "AuthServ",
            prefs.getBoolean(IS_LOGIN_KEY, false).toString() + " from checkIfUserLoggedIn"
        )
        return prefs.getBoolean(IS_LOGIN_KEY, false)
    }

    fun logOut() {
        prefs.edit().putBoolean(IS_LOGIN_KEY, false).apply()
        Log.i("Auth-ServLogOut", "Log out from system")
        Log.i("Auth-ServLogOut", "Last login is \"${prefs.getString(LAST_LOGIN_KEY, "")}\"")
    }

    fun getRowByLogin(login: String): UserLogInInfo? {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            UserTokenContract.UserTokenEntry.COLUMN_NICKNAME,
            UserTokenContract.UserTokenEntry.COLUMN_LOGIN,
            UserTokenContract.UserTokenEntry.COLUMN_TOKEN,
            UserTokenContract.UserTokenEntry.COLUMN_EXPIRE,
            UserTokenContract.UserTokenEntry.COLUMN_NICKNAME
        )

        val selection = "${UserTokenContract.UserTokenEntry.COLUMN_LOGIN}=?"
        val selectionArgs = arrayOf(login)

        val cursor = db.query(
            UserTokenContract.UserTokenEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val items = mutableListOf<UserLogInInfo>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        with(cursor) {
            while (moveToNext()) {
                val nickname =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_NICKNAME))
                val loginU =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_LOGIN))
                val token =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_TOKEN))
                val expire =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_EXPIRE))
                items.add(
                    UserLogInInfo(
                        nickname,
                        loginU,
                        token,
                        LocalDateTime.parse(expire, formatter).plusMinutes(10)
                    )
                )
            }
        }
        cursor.close()

        for (item in items) {
            Log.i("Auth-readDbByLogin", item.toString())
        }

        return if (items.isEmpty()) {
            null
        } else {
            items[0]
        }
    }

    //TODO: remove when dev to be ended
    private fun readData() {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            UserTokenContract.UserTokenEntry.COLUMN_NICKNAME,
            UserTokenContract.UserTokenEntry.COLUMN_LOGIN,
            UserTokenContract.UserTokenEntry.COLUMN_TOKEN,
            UserTokenContract.UserTokenEntry.COLUMN_EXPIRE
        )
        val cursor = db.query(
            UserTokenContract.UserTokenEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        val itemIds = mutableListOf<Long>()
        val items = mutableListOf<UserLogInInfo>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                itemIds.add(itemId)
                val nickname =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_NICKNAME))
                val login =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_LOGIN))
                val token =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_TOKEN))
                val expire =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_EXPIRE))
                items.add(
                    UserLogInInfo(
                        nickname,
                        login,
                        token,
                        LocalDateTime.parse(expire, formatter).plusMinutes(10)
                    )
                )
            }
        }
        cursor.close()

        for (item in items) {
            Log.i("Auth-readDb", item.toString())
        }
    }

    private suspend fun dropTable() {
        Log.i("AuthServ-drop", "Table dropped")
        dbHelper.writableDatabase.execSQL("DROP TABLE IF EXISTS ${UserTokenContract.UserTokenEntry.TABLE_NAME}")
    }
}