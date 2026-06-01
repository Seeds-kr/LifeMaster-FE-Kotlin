package com.example.lifemaster.presentation.total.detox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.databinding.ActivityDetoxPermanentBlockBinding

class DetoxPermanentBlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetoxPermanentBlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetoxPermanentBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackPomodoro.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("openDetox", true)
            }

            startActivity(intent)
            finish()
        }
    }
}