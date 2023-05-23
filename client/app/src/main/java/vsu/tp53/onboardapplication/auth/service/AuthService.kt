package vsu.tp53.onboardapplication.auth.service

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.provider.BaseColumns
import android.util.Log
import androidx.preference.PreferenceManager
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.model.domain.User
import vsu.tp53.onboardapplication.model.domain.UserLogInInfo
import vsu.tp53.onboardapplication.model.entity.UserRegAuthorize
import vsu.tp53.onboardapplication.model.entity.UserRegAuthorizePost
import vsu.tp53.onboardapplication.model.entity.UserToken
import vsu.tp53.onboardapplication.model.entity.UserTokenPost
import vsu.tp53.onboardapplication.sqlitedb.UserTokenContract
import vsu.tp53.onboardapplication.sqlitedb.UserTokenDbHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val IS_LOGIN_KEY = "IsLogin"
private const val LAST_LOGIN = "LastLogin"

class AuthService(
    private val restTemplate: RestTemplate,
    val context: Context
) {
    private val regUrl: String = "http://192.168.0.101:8000/register/"
    private val authUrl: String = "http://192.168.0.101:8000/authorize/"
    private val tokenAuthUrl: String = "http://192.168.0.101:8000/token_authorize/"
    private val dbHelper = UserTokenDbHelper(context)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun registerUser(user: User) {
//        dropTable()
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

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(UserTokenContract.UserTokenEntry.COLUMN_LOGIN, user.login)
            put(UserTokenContract.UserTokenEntry.COLUMN_TOKEN, userToken.idToken)
            put(
                UserTokenContract.UserTokenEntry.COLUMN_EXPIRE,
                formatter.format(LocalDateTime.now()).toString()
            )
        }
        val newRowId = db?.insert(UserTokenContract.UserTokenEntry.TABLE_NAME, null, values)
        Log.i("Auth-newRowId", newRowId.toString())

        prefs.edit().putBoolean(IS_LOGIN_KEY, true).apply()
        prefs.edit().putString(LAST_LOGIN, user.login).apply()
        Log.i("Auth-ServReg", "Last login is \"${prefs.getString(LAST_LOGIN, "")}\"")
        readData()
    }

    fun authorizeUser(user: User) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val userRegAuthorizePost: UserRegAuthorizePost = user.mapToEntity()
        Log.i("AuthServ-authObj", userRegAuthorizePost.toString())

        restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
        val userToken = restTemplate.postForObject(
            authUrl,
            userRegAuthorizePost,
            UserToken::class.java
        )!!

        Log.i("Auth-authSuc", userToken.toString())

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(UserTokenContract.UserTokenEntry.COLUMN_LOGIN, user.login)
            put(UserTokenContract.UserTokenEntry.COLUMN_TOKEN, userToken.idToken)
            put(
                UserTokenContract.UserTokenEntry.COLUMN_EXPIRE,
                formatter.format(LocalDateTime.now()).toString()
            )
        }
        val selection = "${UserTokenContract.UserTokenEntry.COLUMN_LOGIN} LIKE ?"
        val selectionArgs = arrayOf(user.login)
        val newRowId = db?.update(
            UserTokenContract.UserTokenEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )
        Log.i("Auth-updatedRowId", newRowId.toString())

        prefs.edit().putBoolean(IS_LOGIN_KEY, true).apply()
        prefs.edit().putString(LAST_LOGIN, user.login).apply()
        Log.i("Auth-ServAuth", "Last login is \"${prefs.getString(LAST_LOGIN, "")}\"")
        readData()
    }

    fun authToken() {
        val lastUserToken: String? = getRowByLogin(prefs.getString(LAST_LOGIN, "")!!)?.tokenId
        if (lastUserToken != null) {
            val userTokenPost = UserTokenPost(lastUserToken)
            restTemplate.messageConverters.add(MappingJacksonHttpMessageConverter())
            val userRegAuth = restTemplate.postForObject(
                tokenAuthUrl,
                userTokenPost,
                UserRegAuthorize::class.java
            )!!

            //TODO: finish function
        }
    }

    fun checkTokenIsNotExpired(): Boolean {
        val lastUser: UserLogInInfo? = getRowByLogin(prefs.getString(LAST_LOGIN, "")!!)
        return if (lastUser == null) {
            prefs.edit().putBoolean(IS_LOGIN_KEY, false).apply()
            false
        } else {
            val expire = lastUser.expire
            val now = LocalDateTime.now()
            if (now.isAfter(expire)) {
                prefs.edit().putBoolean(IS_LOGIN_KEY, false).apply()
            }
            !now.isAfter(expire)
        }
    }

    fun checkIfUserLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGIN_KEY, false)
    }

    fun logOut() {
        prefs.edit().putBoolean(IS_LOGIN_KEY, false).apply()
        Log.i("Auth-ServLogOut", "Log out from system")
        Log.i("Auth-ServLogOut", "Last login is \"${prefs.getString(LAST_LOGIN, "")}\"")
    }

    private fun getRowByLogin(login: String): UserLogInInfo? {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            UserTokenContract.UserTokenEntry.COLUMN_LOGIN,
            UserTokenContract.UserTokenEntry.COLUMN_TOKEN,
            UserTokenContract.UserTokenEntry.COLUMN_EXPIRE
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
                val loginU =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_LOGIN))
                val token =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_TOKEN))
                val expire =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_EXPIRE))
                items.add(
                    UserLogInInfo(
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
                val login =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_LOGIN))
                val token =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_TOKEN))
                val expire =
                    getString(getColumnIndexOrThrow(UserTokenContract.UserTokenEntry.COLUMN_EXPIRE))
                items.add(
                    UserLogInInfo(
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

    private fun dropTable() {
        Log.i("AuthServ-drop", "Table dropped")
        dbHelper.writableDatabase.execSQL("DROP TABLE IF EXISTS ${UserTokenContract.UserTokenEntry.TABLE_NAME}")
    }
}