import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R

class AnswerAdapter(private val mAnswers: List<String>) :
    RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val nameTextView = itemView.findViewById<TextView>(R.id.answer_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.item_contact, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: AnswerAdapter.ViewHolder, position: Int) {
        val answer: String = mAnswers[position]
        val textView = viewHolder.nameTextView
        textView.text = answer
    }

    override fun getItemCount(): Int {
        return mAnswers.size
    }
}