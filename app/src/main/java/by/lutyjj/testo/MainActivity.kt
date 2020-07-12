package by.lutyjj.testo

import AnswerAdapter
import MyItemDetailsLookup
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R


class MainActivity : AppCompatActivity() {
    var tracker: SelectionTracker<Long>? = null
    private val adapter = AnswerAdapter()
    private lateinit var list: ArrayList<String>
    private var totalQuestions: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = DatabaseHelper(this)
        val question = findViewById<TextView>(R.id.questionTitle)

        val rvAnswers = findViewById<View>(R.id.rvAnswers) as RecyclerView
        rvAnswers.adapter = adapter
        adapter.list = updateList(db)
        adapter.notifyDataSetChanged()

        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.default_padding).toInt()
            )
        )

        tracker = SelectionTracker.Builder<Long>(
            "mySelection",
            rvAnswers,
            StableIdKeyProvider(rvAnswers),
            MyItemDetailsLookup(rvAnswers),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        adapter.tracker = tracker
        startTimer()
        setTotalQuestions()

        val skipBtn = findViewById<Button>(R.id.skip)
        skipBtn.setOnClickListener {
            adapter.list.clear()
            val list = adapter.getSelectedAnsList()
            adapter.selectedList.clear()
            adapter.list.addAll(updateList(db))
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