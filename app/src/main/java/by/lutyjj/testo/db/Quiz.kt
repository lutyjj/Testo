package by.lutyjj.testo.db

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Entity
data class Quiz(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "total_questions") val totalQuestions: Int,
    @ColumnInfo(name = "answered_questions_count") val answeredQuestionsCount: Int,
    @ColumnInfo(name = "answered_questions") val answeredQuestions: String?,
    @ColumnInfo(name = "update_date") val updateDate: Date
)

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz")
    fun getAll(): LiveData<List<Quiz>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quiz: Quiz)

    @Query("DELETE FROM quiz")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM quiz")
    suspend fun getSize(): Int

    @Query("SELECT name FROM quiz")
    suspend fun getNames(): List<String>

    @Update
    suspend fun update(quiz: Quiz)
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(entities = [Quiz::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "word_database"
                ).addCallback(
                    QuizDbCallback(
                        scope,
                        context
                    )
                ).build()
                INSTANCE = instance
                return instance
            }
        }

        private class QuizDbCallback(
            private val scope: CoroutineScope,
            private val context: Context
        ) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        scanFolder(database.quizDao())
                    }
                }
            }

            suspend fun scanFolder(quizDao: QuizDao) {
                if (quizDao.getSize() == 0) {
                    val files = context.getExternalFilesDir(null)?.listFiles()
                    files?.let {
                        val list = it.map { file -> file.name } as List<String>

                        for (file in list) {
                            val path = context.getExternalFilesDir(null)?.absolutePath + "/" + file
                            val db = SQLiteDatabase.openDatabase(
                                path, null,
                                SQLiteDatabase.OPEN_READONLY
                            )
                            val cursor = db.rawQuery("SELECT * FROM questions", null)
                            val test = Quiz(
                                file,
                                cursor.count,
                                0,
                                null,
                                Date()
                            )
                            cursor.close()
                            quizDao.insert(test)
                        }
                    }
                }
            }
        }
    }
}

class QuizRepository(private val quizDao: QuizDao) {
    val allQuizzes: LiveData<List<Quiz>> = quizDao.getAll()

    suspend fun insert(quiz: Quiz) {
        quizDao.insert(quiz)
    }

    suspend fun deleteAll() {
        quizDao.deleteAll()
    }

    suspend fun update(quiz: Quiz) {
        quizDao.update(quiz)
    }
}

class TestViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: QuizRepository
    val allQuizzes: LiveData<List<Quiz>>

    init {
        val testsDao = AppDatabase.getDatabase(
            application,
            viewModelScope
        ).quizDao()
        repository = QuizRepository(testsDao)
        allQuizzes = repository.allQuizzes
    }

    fun insert(quiz: Quiz) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(quiz)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun update(quiz: Quiz) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(quiz)
    }
}