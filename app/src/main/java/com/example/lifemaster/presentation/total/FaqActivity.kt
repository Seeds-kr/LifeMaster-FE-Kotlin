package com.example.lifemaster.presentation.total

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R

class FaqActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        val cats = resources.getStringArray(R.array.faq_categories)
        val qs = resources.getStringArray(R.array.faq_questions)
        val answers = resources.getStringArray(R.array.faq_answers)
        val items = qs.indices.map { i ->
            FaqItem(
                category = cats.getOrNull(i).orEmpty(),
                question = qs[i],
                answer = answers[i],
            )
        }

        findViewById<RecyclerView>(R.id.rvFaq).apply {
            layoutManager = LinearLayoutManager(this@FaqActivity)
            adapter = FaqAdapter(items)
        }
    }
}
