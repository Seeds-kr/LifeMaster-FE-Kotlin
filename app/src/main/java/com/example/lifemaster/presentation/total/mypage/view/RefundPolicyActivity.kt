package com.example.lifemaster.presentation.total.mypage.view

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R

class RefundPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refund_policy)

        // 간단한 구현: 화면 내 닫기/뒤로 버튼이 없으므로 시스템 뒤로가기 사용을 기본으로 합니다.
        val titleView = findViewById<TextView>(R.id.tvRefundPolicyTitle)
        titleView.text = getString(R.string.payment_refund_cancel_policy)
    }
}

