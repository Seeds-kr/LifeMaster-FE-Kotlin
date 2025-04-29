package com.example.lifemaster.presentation.group.view.activity

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toLowerCase
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lifemaster.databinding.ActivityAlarmRingsBinding
import com.example.lifemaster.presentation.group.view.service.AlarmService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingsActivity : AppCompatActivity() {

    lateinit var binding: ActivityAlarmRingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlarmRingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentTimeMillis = System.currentTimeMillis()
        val date = Date(currentTimeMillis)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val result = formatter.format(date)
        val hour = result.substringBefore(":").toInt()
        val timeType = when(hour) {
            in 6..11 -> "Morning"
            in 12..18 -> "Afternoon"
            in 19..23 -> "Night"
            else -> "Dawn"
        }

        binding.tvTimeType.text = timeType

        val initialPaddingLeft = binding.main.paddingLeft
        val initialPaddingTop = binding.main.paddingTop
        val initialPaddingRight = binding.main.paddingRight
        val initialPaddingBottom = binding.main.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(initialPaddingLeft+systemBars.left, initialPaddingTop+systemBars.top, initialPaddingRight+systemBars.right, initialPaddingBottom+systemBars.bottom)
            insets
        }

        initListeners()
    }

    private fun initListeners() = with(binding) {
        tvStopAlarm.setOnClickListener {
            // 서비스 중단
            Toast.makeText(this@AlarmRingsActivity, "알람이 종료되었습니다!", Toast.LENGTH_SHORT).show()
            val serviceIntent = Intent(this@AlarmRingsActivity, AlarmService::class.java)
            stopService(serviceIntent)
            finish()
        }
        // 뒤로가기 버튼(백버튼) 막기
        onBackPressedDispatcher.addCallback(
            this@AlarmRingsActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(this@AlarmRingsActivity, "뒤로 가기로 앱을 종료할 수 없습니다!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}