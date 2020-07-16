package by.lutyjj.testo

import AnswerAdapter
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lutyjj.testo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val adapter = AnswerAdapter()
    private var totalQuestions: Int = 0
    private var totalMistakes: Int = 0
    private var totalCorrect: Int = 0
    private var currentQuestion: Int = 0
    private var isFabReady: Boolean = false
    private var answeredQuestions: ArrayList<Int> = ArrayList()

    private lateinit var db: DatabaseHelper
    private lateinit var answerList: ArrayList<String>
    private lateinit var correctList: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvAnswers = findViewById<View>(R.id.rvAnswers) as RecyclerView
        rvAnswers.adapter = adapter
        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_padding).toInt()))

        db = DatabaseHelper(this)
        setTimer()
        updateQuestion()

        val skipBtn = findViewById<Button>(R.id.skip)
        skipBtn.setOnClickListener { updateQuestion() }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{
            if (isFabReady) {
                fab.setImageResource(R.drawable.ic_baseline_done_outline_24)
                fab.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                resetCorrectAnswers()
                updateQuestion()
            }
            else {
                val list = adapter.selectedList
                list.sort()
                highlightAnswers()
                if (correctList == list) {
                    isFabReady
                    answeredQuestions.add(currentQuestion)
                    fab.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.fabGreen))
                    totalCorrect++
                }
                else {
                    fab.setImageResource(R.drawable.ic_close_outline)
                    fab.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.fabRed))
                    totalMistakes++
                }
            }
            isFabReady = !isFabReady
        }
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
        val countUpTimer = object : CountDownTimer(Long.MAX_VALUE,1000) {
            var seconds = 0
            var minutes = 0

            override fun onFinish() {
                seconds = 0
                minutes = 0
            }

            override fun onTick(millisUntilFinished: Long) {
                if (seconds < 10)
                    timer.text = "$minutes:0$seconds"
                else
                    timer.text = "$minutes:$seconds"
                seconds++
                if (seconds == 60) {
                    seconds = 0
                    minutes++
                }
            }
        }
        countUpTimer.start()
    }

    private fun updateData() {
        val question = findViewById<TextView>(R.id.questionTitle)
        val questionCursor = db.questions
        totalQuestions = questionCursor.count
        updateQuestionCounter()

        var questionIndex = (1 .. totalQuestions).random()
        while (answeredQuestions.contains(questionIndex)) {
            questionIndex = (1 .. totalQuestions).random()
        }

        currentQuestion = questionIndex
        questionCursor.moveToPosition(questionIndex - 1)
        question.text = questionCursor.getString(1)

        val c = db.getAnswers(questionIndex)
        val answerList: ArrayList<String> = ArrayList()
        val correctList: ArrayList<Int> = ArrayList()
        do {
            answerList.add(c.getString(0))
            val isCorrect = c.getInt(1)
            if (isCorrect == 1)
                correctList.add(c.position)
        } while (c.moveToNext())

        this.answerList = answerList
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
        questionCounter.text = "$totalCorrect/$totalQuestions"
    }
}