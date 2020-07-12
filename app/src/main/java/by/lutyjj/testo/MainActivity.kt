package by.lutyjj.testo

import AnswerAdapter
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.sql.Time


class MainActivity : AppCompatActivity() {
    private lateinit var list: ArrayList<String>
    private var totalQuestions: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = DatabaseHelper(this)
        val question = findViewById<TextView>(R.id.questionTitle)
        list = updateList(db)
        val rvAnswers = findViewById<View>(R.id.rvAnswers) as RecyclerView
        val adapter = AnswerAdapter(list)
        rvAnswers.adapter = adapter
        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.addItemDecoration(MarginItemDecoration(
            resources.getDimension(R.dimen.default_padding).toInt()))

        startTimer()
        setTotalQuestions()

        val skipBtn = findViewById<Button>(R.id.skip)
        skipBtn.setOnClickListener{
            list.clear()
            list.addAll(updateList(db))
            val animation = AnimationUtils.loadAnimation(this, R.anim.item_animation_fall_down)
            question.startAnimation(animation)
            rvAnswers.scheduleLayoutAnimation()
            adapter.notifyDataSetChanged()
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

    private fun updateList(db: DatabaseHelper): ArrayList<String> {
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

        return answerList
    }
}