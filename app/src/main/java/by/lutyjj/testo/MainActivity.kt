package by.lutyjj.testo

import AnswerAdapter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val db = DatabaseHelper(this)
        val questionIndex = 6

        val question = findViewById<TextView>(R.id.questionTitle)
        val questionCursor = db.questions
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

        val rvAnswers = findViewById<View>(R.id.rvAnswers) as RecyclerView
        val adapter = AnswerAdapter(answerList)
        rvAnswers.adapter = adapter
        rvAnswers.layoutManager = LinearLayoutManager(this)
    }
}