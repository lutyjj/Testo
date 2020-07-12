import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.R

class AnswerAdapter : RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {
    var list: ArrayList<String> = ArrayList()
    var tracker: SelectionTracker<Long>? = null
    var selectedList: ArrayList<Int> = ArrayList()

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val number = list[position]
        tracker?.let {
            holder.bind(number, it.isSelected(position.toLong()))
            if (it.isSelected(position.toLong()))
                if (selectedList.contains(position))
                    selectedList.remove(position)
                else selectedList.add(position)
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
    override fun getItemId(position: Int): Long = position.toLong()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var text: TextView = view.findViewById(R.id.answer_title)

        fun bind(value: String, isActivated: Boolean = false) {
            text.text = value
            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }

    }
}