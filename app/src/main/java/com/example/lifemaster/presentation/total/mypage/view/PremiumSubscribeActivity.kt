package com.example.lifemaster.presentation.total.mypage.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R

/**
 * 프리미엄 구독 결제 화면 (UI만 구현, 실제 결제 기능 없음)
 */
class PremiumSubscribeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_subscribe)

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnStartPremium).setOnClickListener {
            // 결제 기능은 추후 구현
        }
    }
}
