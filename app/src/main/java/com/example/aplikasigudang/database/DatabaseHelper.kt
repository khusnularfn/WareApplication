package com.example.aplikasigudang.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.aplikasigudang.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_PASSWORD TEXT)"
        db?.execSQL(createTableQuery)

        // Insert initial data (5 users) for testing
        val users = listOf(
            User(username = "divisi1", password = "admin1"),
            User(username = "divisi2", password = "admin2"),
            User(username = "divisi3", password = "admin3"),
            User(username = "divisi4", password = "admin4"),
            User(username = "divisi5", password = "admin5"),
            User(username = "stafftraksi", password = "admin6"),
            User(username = "manajer", password = "manajer"),
            User(username = "ktu", password = "adminktu")
        )

        for (user in users) {
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, user.username)
                put(COLUMN_PASSWORD, user.password)
            }
            db?.insert(TABLE_USERS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_USERS"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    @SuppressLint("Range")
    fun getUserByUsername(username: String): User? {
        val db = this.readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null)
        return if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            val password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD))
            User(id, username, password)
        } else {
            null
        }
    }
}
