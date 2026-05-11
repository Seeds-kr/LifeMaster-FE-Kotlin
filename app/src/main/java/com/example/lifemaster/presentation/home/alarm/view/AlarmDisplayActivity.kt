package com.example.lifemaster.presentation.home.alarm.view

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityAlarmDisplayBinding
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmDisplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmDisplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showOnLockScreen()
        binding = ActivityAlarmDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val alarmItem = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("ALARM_DATA", AlarmModel::class.java)
        } else {
            intent.getParcelableExtra<AlarmModel>("ALARM_DATA")
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.alarmFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val bundle = Bundle().apply {
            putParcelable("ALARM_DATA", alarmItem)
        }
        // navigation graph를 수동으로 설정 + startDestination에 데이터 전달
        navController.setGraph(R.navigation.nav_graph_alarm, bundle)
    }

    /**
     * 잠금 화면 위로 액티비티를 띄우기 위한 설정
     */
    private fun showOnLockScreen() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null) // Q. 설명 필요
        } else {
            window.addFlags( // Q. 설명 필요
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}