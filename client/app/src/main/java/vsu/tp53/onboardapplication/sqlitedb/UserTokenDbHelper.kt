package vsu.tp53.onboardapplication.sqlitedb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

object UserTokenContract {
    object UserTokenEntry : BaseColumns {
        const val TABLE_NAME = "user_token"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_LOGIN = "login"
        const val COLUMN_TOKEN = "token"
        const val COLUMN_EXPIRE = "expire"
    }
}

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE IF  NOT EXISTS ${UserTokenContract.UserTokenEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${UserTokenContract.UserTokenEntry.COLUMN_NICKNAME} TEXT UNIQUE," +
            "${UserTokenContract.UserTokenEntry.COLUMN_LOGIN} TEXT UNIQUE," +
            "${UserTokenContract.UserTokenEntry.COLUMN_TOKEN} TEXT)"

private const val SQL_DELETE_TABLE =
    "DROP TABLE IF EXISTS ${UserTokenContract.UserTokenEntry.TABLE_NAME}"

private const val SQL_ALTER_TABLE_ADD_EXPIRE =
    "ALTER TABLE ${UserTokenContract.UserTokenEntry.TABLE_NAME}" +
            " ADD COLUMN ${UserTokenContract.UserTokenEntry.COLUMN_EXPIRE} TEXT;"

class UserTokenDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(p0: SQLiteDatabase?) {
        Log.i(UserTokenDbHelper::class.java.name, "Creating db")
        p0?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        Log.i(UserTokenDbHelper::class.java.name, "Upgrade db from $p1 to $p2")
        p0?.execSQL(SQL_DELETE_TABLE)
        onCreate(p0)
        p0?.execSQL(SQL_ALTER_TABLE_ADD_EXPIRE)
    }

    companion object {
        const val DATABASE_VERSION = 7
        const val DATABASE_NAME = "UserToken.db"
    }
}