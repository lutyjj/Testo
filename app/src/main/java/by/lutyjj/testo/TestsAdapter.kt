package by.lutyjj.testo

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lutyjj.testo.R
import kotlinx.android.synthetic.main.item_test.view.*

class TestsAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<TestsAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var tests = emptyList<Test>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.item_test, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val test = tests[position]
        val item = holder.itemView
        val context = item.context

        item.test_title.text = test.name
        item.total_questions.text = context.getString(R.string.total_questions, test.totalQuestions)
        item.update_date.text = SimpleDateFormat("dd MMMM, yyyy").format(test.updateDate)
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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val view = view
    }
}