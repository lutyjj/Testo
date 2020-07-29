package by.lutyjj.testo

import android.content.res.ColorStateList
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.lutyjj.testo.adapters.QuizAdapter
import by.lutyjj.testo.db.Quiz
import by.lutyjj.testo.db.TestViewModel
import by.lutyjj.testo.decorators.MarginItemDecoration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.lutyjj.testo.R
import java.util.*
import kotlin.collections.ArrayList

class QuizActivity() : AppCompatActivity() {
    private val adapter = QuizAdapter()
    private var totalQuestions: Int = 0
    private var totalMistakes: Int = 0
    private var currentQuestion: Int = 0
    private var isFabReady: Boolean = false
    private var answeredQuestions: ArrayList<Int> = ArrayList()

    private lateinit var db: SQLiteDatabase
    private lateinit var answerList: ArrayList<String>
    private lateinit var correctList: ArrayList<Int>
    private lateinit var dbName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val rvAnswers = findViewById<View>(R.id.rvAnswers) as RecyclerView
        rvAnswers.adapter = adapter
        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.default_padding).toInt()
            )
        )

        dbName = intent.getStringExtra("db_name")!!
        val path = getExternalFilesDir(null)?.absolutePath + "/" +
                dbName
        db = SQLiteDatabase.openDatabase(
            path, null,
            SQLiteDatabase.OPEN_READONLY
        )

        setTimer()
        updateQuestion()

        val skipBtn = findViewById<Button>(R.id.skip_button)
        skipBtn.setOnClickListener { updateQuestion() }

        val saveBtn = findViewById<Button>(R.id.save_button)
        saveBtn.setOnClickListener { saveState() }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            if (isFabReady) {
                fab.setImageResource(R.drawable.ic_baseline_done_outline_24)
                fab.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.colorAccent))
                resetCorrectAnswers()
                updateQuestion()
            } else {
                val list = adapter.selectedList
                list.sort()
                highlightAnswers()
                if (correctList == list) {
                    answeredQuestions.add(currentQuestion)
                    fab.backgroundTintList =
                        ColorStateList.valueOf(getColor(R.color.fabGreen))
                } else {
                    fab.setImageResource(R.drawable.ic_close_outline)
                    fab.backgroundTintList =
                        ColorStateList.valueOf(getColor(R.color.fabRed))
                    totalMistakes++
                }
            }
            isFabReady = !isFabReady
        }
    }

    private fun restoreState() {
        val answered = Gson().fromJson(intent.getStringExtra("answered_questions"), answeredQuestions::class.java)
        answeredQuestions = answered
    }

    private fun saveState() {
        val testRepo = TestViewModel(this.application)
        val answersJson = Gson().toJson(answeredQuestions)

        testRepo.update(Quiz(dbName, totalQuestions, answeredQuestions.size, answersJson, Date()))
        finish()
    }

    private fun highlightAnswers() {
        adapter.correctList = correctList
        for (i in correctList) adapter.notifyItemChanged(i)
    }

    private fun resetCorrectAnswers() {
        adapter.correctList.clear()
    }

    private fun setTimer() {
        val timer = findViewById<TextView>(R.id.timer)
        val countUpTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            var seconds = 0
            var minutes = 0

            override fun onFinish() {
                seconds = 0
                minutes = 0
            }

            override fun onTick(millisUntilFinished: Long) {
                if (seconds < 10)
                    timer.text = getString(R.string.timer_under_10_min, minutes, seconds)
                else
                    timer.text = getString(R.string.timer_over_10_min, minutes, seconds)
                seconds++
                if (seconds == 60) {
                    seconds = 0
                    minutes++
                }
            }
        }
        countUpTimer.start()
    }

    private fun getQuestions(): Cursor {
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

    private fun getAnswers(questionIndex: Int): Cursor {
        val qb = SQLiteQueryBuilder()
        val sqlSelect = arrayOf("answer_text", "is_correct")
        val sqlTables = "answers"
        qb.tables = sqlTables
        val cursor = qb.query(
            db, sqlSelect, "question_id = $questionIndex", null,
            null, null, null
        )
        cursor.moveToFirst()
        return cursor
    }

    private fun updateData() {
        val question = findViewById<TextView>(R.id.questionTitle)
        val questionCursor = getQuestions()
        totalQuestions = questionCursor.count
        updateQuestionCounter()

        var questionIndex = (1..totalQuestions).random()
        if (answeredQuestions.size == totalQuestions) {
            saveState()
            finish()
        } else {
            while (answeredQuestions.contains(questionIndex))
                questionIndex = (1..totalQuestions).random()
        }

        currentQuestion = questionIndex
        questionCursor.moveToPosition(questionIndex - 1)
        question.text = questionCursor.getString(1)

        val c = getAnswers(questionIndex)

        val answerList: ArrayList<Pair<String, Int>> = ArrayList()
        do answerList.add(c.getString(0) to c.getInt(1))
        while (c.moveToNext())

        answerList.shuffle()

        val correctList = ArrayList<Int>()
        for ((index, answer) in answerList.unzip().second.withIndex())
            if (answer == 1)
                correctList.add(index)

        this.answerList = answerList.unzip().first as ArrayList<String>
        this.correctList = correctList
    }

    private fun updateQuestion() {
        val question = findViewById<TextView>(R.id.questionTitle)
        val mistakesTv = findViewById<TextView>(R.id.mistakes_counter)
        val rvAnswers = findViewById<RecyclerView>(R.id.rvAnswers)

        mistakesTv.text = totalMistakes.toString()
        adapter.list.clear()
        adapter.selectedList.clear()
        updateData()
        adapter.list.addAll(answerList)
        val animation = AnimationUtils.loadAnimation(this, R.anim.item_animation)
        question.startAnimation(animation)
        rvAnswers.scheduleLayoutAnimation()
        adapter.notifyDataSetChanged()
    }

    private fun updateQuestionCounter() {
        val questionCounter = findViewById<TextView>(R.id.question_counter)
        questionCounter.text =
            getString(R.string.question_counter, answeredQuestions.size, totalQuestions)
    }
}