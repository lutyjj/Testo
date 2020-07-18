package by.lutyjj.testo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.lutyjj.testo.adapters.TestAdapter
import by.lutyjj.testo.db.TestViewModel
import by.lutyjj.testo.decorators.MarginItemDecoration
import com.lutyjj.testo.R

class MainActivity : AppCompatActivity() {
    private lateinit var testViewModel: TestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvAnswers = findViewById<View>(R.id.rvTests) as RecyclerView
        val adapter = TestAdapter(this)
        rvAnswers.adapter = adapter
        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.extended_padding).toInt()
            )
        )

        testViewModel = ViewModelProvider(this).get(TestViewModel::class.java)
        testViewModel.allTests.observe(this, Observer { tests ->
            tests?.let { adapter.setTests(it) }
        })
    }
}
