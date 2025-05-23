package com.deakin.llm_quiz_app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getIntOrNull
import com.deakin.llm_quiz_app.model.User
import com.deakin.llm_quiz_app.util.Util
import androidx.core.database.sqlite.transaction
import com.deakin.llm_quiz_app.model.Question

class DatabaseHelper(
    context: Context?,
    factory: SQLiteDatabase.CursorFactory?
) : SQLiteOpenHelper(context, Util.DATABASE_NAME, factory, Util.DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE ${Util.USER_TABLE_NAME} (
                ${Util.USER_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Util.USERNAME} TEXT,
                ${Util.EMAIL} TEXT,
                ${Util.PASSWORD} TEXT
            )
        """.trimIndent()

        val createInterestsTable = """
            CREATE TABLE ${Util.USER_INTEREST_TABLE_NAME} (
                ${Util.INTEREST_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Util.USER_ID} INTEGER,
                ${Util.INTEREST} TEXT
            )
        """.trimIndent()

        val createQuestionsTable = """
            CREATE TABLE ${Util.QUESTION_TABLE_NAME} (
                ${Util.QUESTION_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Util.USER_ID} INTEGER,
                ${Util.QUESTION} TEXT,
                ${Util.OPTION_A} TEXT,
                ${Util.OPTION_B} TEXT,
                ${Util.OPTION_C} TEXT,
                ${Util.OPTION_D} TEXT,
                ${Util.SELECTED_OPTION} INTEGER,
                ${Util.CORRECT_ANSWER} INTEGER
            )
        """.trimIndent()

        sqLiteDatabase.execSQL(createUserTable)
        sqLiteDatabase.execSQL(createInterestsTable)
        sqLiteDatabase.execSQL(createQuestionsTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropUserTable = "DROP TABLE IF EXISTS ${Util.USER_TABLE_NAME}"
        val dropInterestsTable = "DROP TABLE IF EXISTS ${Util.USER_INTEREST_TABLE_NAME}"

        sqLiteDatabase.execSQL(dropUserTable)
        sqLiteDatabase.execSQL(dropInterestsTable)

        onCreate(sqLiteDatabase)
    }

    fun insertUser(user: User): Long {
        if (userAlreadyExists(user.username)) {
            return -1
        }

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues: ContentValues = ContentValues().apply {
            put(Util.USERNAME, user.username)
            put(Util.EMAIL, user.email)
            put(Util.PASSWORD, user.password)
        }

        val newRowId: Long = db.insert(
            /* table = */ Util.USER_TABLE_NAME,
            /* nullColumnHack = */ null,
            /* values = */ contentValues)
        db.close()

        return newRowId
    }

    fun userAlreadyExists(usernameOrEmail: String): Boolean {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var userExists = false

        try {
            cursor = db.query(
                Util.USER_TABLE_NAME,
                arrayOf(Util.USER_ID),
                "${Util.USERNAME} = ? OR ${Util.EMAIL} = ?",
                arrayOf(usernameOrEmail, usernameOrEmail),
                null,
                null,
                null
            )

            userExists = cursor.count > 0
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return userExists
    }

    fun getUserInterests(userId: Int): MutableSet<String> {
        val interests = mutableSetOf<String>()
        val db = this.readableDatabase

        val cursor = db.query(
            Util.USER_INTEREST_TABLE_NAME,
            arrayOf(Util.INTEREST),
            "${Util.USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val interest = cursor.getString(cursor.getColumnIndexOrThrow(Util.INTEREST))
                interests.add(interest)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return interests
    }

    fun getUser(userId: Int) : User {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var user = User(username="Guest", email="", password="")

        try {
            cursor = db.query(
                Util.USER_TABLE_NAME,
                arrayOf(Util.USERNAME, Util.EMAIL),
                "${Util.USER_ID} = ?",
                arrayOf(userId.toString()),
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                user.username = cursor.getString(cursor.getColumnIndexOrThrow(Util.USERNAME))
                user.email = cursor.getString(cursor.getColumnIndexOrThrow(Util.EMAIL))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return user
    }

    fun fetchUser(usernameOrEmail: String, password: String): Int {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var userId = -1

        try {
            cursor = db.query(
                Util.USER_TABLE_NAME,
                arrayOf(Util.USER_ID),
                "(${Util.USERNAME} = ? OR ${Util.EMAIL} = ?) AND ${Util.PASSWORD} = ?",
                arrayOf(usernameOrEmail, usernameOrEmail, password),
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(Util.USER_ID))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return userId
    }

    fun insertUserInterests(userId: Int, interests: Set<String>): Boolean {
        if (interests.isEmpty()) return false

        val db = this.writableDatabase
        var result = false

        try {
            db.beginTransaction()

            db.delete(
                Util.USER_INTEREST_TABLE_NAME,
                "${Util.USER_ID} = ?",
                arrayOf(userId.toString())
            )

            for (interest in interests) {
                val contentValues = ContentValues().apply {
                    put(Util.USER_ID, userId)
                    put(Util.INTEREST, interest)
                }
                val rowId = db.insert(Util.USER_INTEREST_TABLE_NAME, null, contentValues)
                Log.d("Row id", rowId.toString())
            }

            db.setTransactionSuccessful()
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
        return result
    }

    fun insertQuestion(question: Question) {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues: ContentValues = ContentValues().apply {
            put(Util.USER_ID, question.userId)
            put(Util.QUESTION, question.question)
            put(Util.OPTION_A, question.optionA)
            put(Util.OPTION_B, question.optionB)
            put(Util.OPTION_C, question.optionC)
            put(Util.OPTION_D, question.optionD)
            put(Util.SELECTED_OPTION, question.selected)
            put(Util.CORRECT_ANSWER, question.correctAnswer)
        }

        db.insert(
            /* table = */ Util.QUESTION_TABLE_NAME,
            /* nullColumnHack = */ null,
            /* values = */ contentValues)
        db.close()
    }

    fun getUserQuestionsHistory(userId: Int): List<Question> {
        val db = this.readableDatabase
        val questions = mutableListOf<Question>()
        var cursor: Cursor? = null

        try {
            cursor = db.query(
                Util.QUESTION_TABLE_NAME,
                arrayOf(
                    Util.USER_ID,
                    Util.QUESTION,
                    Util.OPTION_A,
                    Util.OPTION_B,
                    Util.OPTION_C,
                    Util.OPTION_D,
                    Util.SELECTED_OPTION,
                    Util.CORRECT_ANSWER
                ),
                "${Util.USER_ID} = ?",
                arrayOf(userId.toString()),
                null,
                null,
                null
            )

            while (cursor.moveToNext()) {
                val question = Question(
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(Util.USER_ID)),
                    question = cursor.getString(cursor.getColumnIndexOrThrow(Util.QUESTION)),
                    optionA = cursor.getString(cursor.getColumnIndexOrThrow(Util.OPTION_A)),
                    optionB = cursor.getString(cursor.getColumnIndexOrThrow(Util.OPTION_B)),
                    optionC = cursor.getString(cursor.getColumnIndexOrThrow(Util.OPTION_C)),
                    optionD = cursor.getString(cursor.getColumnIndexOrThrow(Util.OPTION_D)),
                    selected = cursor.getInt(cursor.getColumnIndexOrThrow(Util.SELECTED_OPTION)),
                    correctAnswer = cursor.getInt(cursor.getColumnIndexOrThrow(Util.CORRECT_ANSWER))
                )
                questions.add(question)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return questions
    }
}
