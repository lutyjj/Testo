package by.lutyjj.testo

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

class DatabaseHelper(context: Context?) :
    SQLiteAssetHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {
    val questions: Cursor
        get() {
            val db = readableDatabase
            val qb = SQLiteQueryBuilder()
            val sqlSelect = arrayOf("question_id", "title")
            val sqlTables = "questions"
            qb.tables = sqlTables
            val cursor = qb.query(
                db, sqlSelect, null, null,
                null, null, null
            )
            cursor.moveToFirst()
            return cursor
        }

    fun getAnswers(questionIndex: Int): Cursor {
        val db = readableDatabase
        val qb = SQLiteQueryBuilder()
        val sqlSelect =
            arrayOf("answer_text", "is_correct")
        val sqlTables = "answers"
        qb.tables = sqlTables
        val cursor = qb.query(
            db, sqlSelect, "question_id = $questionIndex", null,
            null, null, null
        )
        cursor.moveToFirst()
        return cursor
    }

    companion object {
        private const val DATABASE_NAME = "test.db"
        private const val DATABASE_VERSION = 1
    }
}