import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R

class AnswerAdapter : RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {
    var list: ArrayList<String> = ArrayList()
    var selectedList: ArrayList<Int> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ansText = list[position]
        val ansView = holder.text
        ansView.isActivated = false
        ansView.text = ansText

        ansView.setOnClickListener{
            if (!ansView.isActivated && !selectedList.contains(position))
                selectedList.add(position)
            else selectedList.remove(position)
            ansView.isActivated = !ansView.isActivated
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_answer, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = list.size

    fun getSelectedAnsList(): ArrayList<Int> = selectedList
    fun clearSelectedAnsList() {selectedList.clear()}

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var text: TextView = view.findViewById(R.id.answer_title)
    }
}