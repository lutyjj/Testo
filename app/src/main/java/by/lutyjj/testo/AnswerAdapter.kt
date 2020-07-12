import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R

class AnswerAdapter(private val mAnswers: List<String>) : RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {
    lateinit var selectedList: ArrayList<Int>
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val answerTitle: TextView = itemView.findViewById(R.id.answer_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val answerView = inflater.inflate(R.layout.item_answer, parent, false)
        return ViewHolder(answerView)
    }

    override fun onBindViewHolder(viewHolder: AnswerAdapter.ViewHolder, position: Int) {
        val answer: String = mAnswers[position]
        val answerTextView = viewHolder.answerTitle
        answerTextView.text = answer
        answerTextView.setOnClickListener{

        }
    }

    override fun getItemCount(): Int {
        return mAnswers.size
    }
}