package com.example.lifemaster.presentation.total.mypage.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R

class LegalDocumentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_document)

        val titleRes = intent.getIntExtra(EXTRA_TITLE_RES, 0)
        val bodyRes = intent.getIntExtra(EXTRA_BODY_RES, 0)
        val titleView = findViewById<TextView>(R.id.tvLegalTitle)
        val bodyView = findViewById<TextView>(R.id.tvLegalBody)
        if (titleRes != 0) titleView.setText(titleRes)
        if (bodyRes != 0) bodyView.setText(bodyRes)
    }

    companion object {
        const val EXTRA_TITLE_RES = "extra_title_res"
        const val EXTRA_BODY_RES = "extra_body_res"

        fun createIntent(context: Context, titleRes: Int, bodyRes: Int): Intent =
            Intent(context, LegalDocumentActivity::class.java).apply {
                putExtra(EXTRA_TITLE_RES, titleRes)
                putExtra(EXTRA_BODY_RES, bodyRes)
            }
    }
}
