package by.lutyjj.testo.adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.lutyjj.testo.TestActivity
import by.lutyjj.testo.db.Test
import com.lutyjj.testo.R

class TestAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<TestAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var tests = emptyList<Test>()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.item_test, parent, false)
        return ViewHolder(itemView)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val test = tests[position]
        val item = holder.itemView

        val testTitle = item.findViewById<TextView>(R.id.test_title)
        val testTotalQuestions = item.findViewById<TextView>(R.id.total_questions)
        val testUpdateDate = item.findViewById<TextView>(R.id.update_date)
        val testProgress = item.findViewById<ProgressBar>(R.id.progress_bar)

        if (test.totalQuestions == 0 || test.completedQuestions == 0) {
            testProgress.progress = 0
        } else {
            testProgress.progress = test.totalQuestions.div(test.completedQuestions)
        }

        testTitle.text = test.name.replace(".db", "")
        testTotalQuestions.text = context.getString(R.string.total_questions, test.totalQuestions)
        testUpdateDate.text = DateFormat.getDateFormat(context).format(test.updateDate)

        item.setOnClickListener {
            val intent = Intent(context, TestActivity::class.java)
            intent.putExtra("db_name", test.name)
            context.startActivity(intent)
        }
    }

    internal fun setTests(tests: List<Test>) {
        this.tests = tests
        notifyDataSetChanged()
    }

    override fun getItemCount() = tests.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}