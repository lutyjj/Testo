package by.lutyjj.testo

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.lutyjj.testo.R

class TestsAdapter : RecyclerView.Adapter<TestsAdapter.ViewHolder>() {
    var list: List<String> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ansText = list[position]
        val answerItem = holder.answerItem
        val answerText = answerItem.findViewById<TextView>(R.id.test_title)
        answerText.text = ansText.replace(".db", "")

        answerItem.setOnClickListener {
            val context = holder.answerItem.context
            val intent = Intent(context, TestActivity::class.java)
            intent.putExtra("db_name", ansText)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_test, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var answerItem: ConstraintLayout = view.findViewById(R.id.test_item_layout)
    }
}