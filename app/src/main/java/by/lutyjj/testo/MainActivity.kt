package by.lutyjj.testo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lutyjj.testo.R

class MainActivity : AppCompatActivity() {
    private val adapter = TestsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvAnswers = findViewById<View>(R.id.rvTests) as RecyclerView
        rvAnswers.adapter = adapter
        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.extended_padding).toInt()
            )
        )

        val files = getExternalFilesDir(null)?.listFiles()
        adapter.list = files?.map { file -> file.name } as List<String>
        adapter.notifyDataSetChanged()
    }
}