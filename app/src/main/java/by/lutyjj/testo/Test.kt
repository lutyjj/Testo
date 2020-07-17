package by.lutyjj.testo

import android.app.Application
import android.content.Context
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
    fun getAllNames(): LiveData<List<Test>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(test: Test)

    @Query("DELETE FROM test")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM test")
    suspend fun getSize(): Int
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}


@Database(entities = [Test::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
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
                ).addCallback(TestDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private class TestDatabaseCallback(private val scope: CoroutineScope) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database.testDao())
                    }
                }
            }

            suspend fun populateDatabase(testDao: TestDao) {
                testDao.deleteAll()
                var test = Test("sk", 111, 0, Date())
                testDao.insert(test)
                test = Test("test", 4, 0, Date())
                testDao.insert(test)
            }
        }
    }
}

class TestRepository(private val testDao: TestDao) {
    val allTests: LiveData<List<Test>> = testDao.getAllNames()

    suspend fun insert(test: Test) {
        testDao.insert(test)
    }
}

class TestViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TestRepository
    val allTests: LiveData<List<Test>>

    init {
        val testsDao = AppDatabase.getDatabase(application, viewModelScope).testDao()
        repository = TestRepository(testsDao)
        allTests = repository.allTests
    }

    fun insert(test: Test) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(test)
    }
}