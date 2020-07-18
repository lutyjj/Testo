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
data class Test(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "total_questions") val totalQuestions: Int,
    @ColumnInfo(name = "completed_questions") val completedQuestions: Int,
    @ColumnInfo(name = "update_date") val updateDate: Date
)

@Dao
interface TestDao {
    @Query("SELECT * FROM test")
    fun getAll(): LiveData<List<Test>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(test: Test)

    @Query("DELETE FROM test")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM test")
    suspend fun getSize(): Int

    @Query("SELECT name FROM test")
    suspend fun getNames(): List<String>
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(entities = [Test::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao

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
                    TestDatabaseCallback(
                        scope,
                        context
                    )
                ).build()
                INSTANCE = instance
                return instance
            }
        }

        private class TestDatabaseCallback(
            private val scope: CoroutineScope,
            private val context: Context
        ) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        scanFolder(database.testDao())
                    }
                }
            }

            suspend fun scanFolder(testDao: TestDao) {
                if (testDao.getSize() == 0) {
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
                            val test = Test(
                                file,
                                cursor.count,
                                0,
                                Date()
                            )
                            cursor.close()
                            testDao.insert(test)
                        }
                    }
                }
            }
        }
    }
}

class TestRepository(private val testDao: TestDao) {
    val allTests: LiveData<List<Test>> = testDao.getAll()

    suspend fun insert(test: Test) {
        testDao.insert(test)
    }

    suspend fun deleteAll() {
        testDao.deleteAll()
    }
}

class TestViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TestRepository
    val allTests: LiveData<List<Test>>

    init {
        val testsDao = AppDatabase.getDatabase(
            application,
            viewModelScope
        ).testDao()
        repository = TestRepository(testsDao)
        allTests = repository.allTests
    }

    fun insert(test: Test) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(test)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
}