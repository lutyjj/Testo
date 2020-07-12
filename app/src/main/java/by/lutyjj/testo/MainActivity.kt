package by.lutyjj.testo

import AnswerAdapter
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val adapter = AnswerAdapter()
    private var totalQuestions: Int = 0
    private var totalMistakes: Int = 0
    private lateinit var answerList: ArrayList<String>
    private lateinit var correctList: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = DatabaseHelper(this)
        val question = findViewById<TextView>(R.id.questionTitle)
        val mistakesTv = findViewById<TextView>(R.id.mistakes_counter)
        val rvAnswers = findViewById<View>(R.id.rvAnswers) as RecyclerView
        rvAnswers.adapter = adapter
        updateList(db)
        adapter.list = answerList
        adapter.notifyDataSetChanged()

        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_padding).toInt()))

        setTotalQuestions()
        startTimer()

        fun nextQuestion() {
            mistakesTv.text = totalMistakes.toString()
            adapter.list.clear()
            adapter.clearSelectedAnsList()
            updateList(db)
            adapter.list.addAll(answerList)
            val animation = AnimationUtils.loadAnimation(this, R.anim.item_animation)
            question.startAnimation(animation)
            rvAnswers.scheduleLayoutAnimation()
            adapter.notifyDataSetChanged()
        }

        val skipBtn = findViewById<Button>(R.id.skip)
        skipBtn.setOnClickListener {
            nextQuestion()
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{
            val list = adapter.getSelectedAnsList()
            list.sort()
            if(correctList == list)
                Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show()
            else {
                totalMistakes++
                Toast.makeText(this, "Wrong", Toast.LENGTH_SHORT).show()
            }
            nextQuestion()
        }
    }

    private fun setTotalQuestions() {
        val questionCounter = findViewById<TextView>(R.id.question_counter)
        questionCounter.text = totalQuestions.toString()
    }

    private fun startTimer() {
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

    private fun updateList(db: DatabaseHelper) {
        val question = findViewById<TextView>(R.id.questionTitle)
        val questionCursor = db.questions
        totalQuestions = questionCursor.count
        val questionIndex = (1 until totalQuestions).random()
        questionCursor.moveToPosition(questionIndex - 1)
        question.text = questionCursor.getString(1)

        val c = db.getAnswers(questionIndex)
        val answerList: ArrayList<String> = ArrayList()
        val correctList: ArrayList<Int> = ArrayList()
        do {
            answerList.add(c.getString(0))
            val isCorrect = c.getInt(1)
            if (isCorrect == 1)
                correctList.add(c.getColumnIndex("is_correct"))
        } while (c.moveToNext())

        this.answerList = answerList
        this.correctList = correctList
    }
}