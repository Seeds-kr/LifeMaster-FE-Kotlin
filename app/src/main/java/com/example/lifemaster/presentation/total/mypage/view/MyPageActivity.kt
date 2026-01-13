package com.example.lifemaster.presentation.total.mypage.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R

class MyPageActivity : AppCompatActivity() {

    private var alertDialog: AlertDialog? = null
    private lateinit var backgroundDimmer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // ID를 통해 뷰 찾기
        val tvEdit = findViewById<TextView>(R.id.tvEdit)
        val tvNickname = findViewById<TextView>(R.id.tvNickname)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnPayment = findViewById<Button>(R.id.btnPayment)
        val tvWithdrawal = findViewById<TextView>(R.id.tvWithdrawal)

        // 블러 처리를 위한 뷰 초기화 및 추가
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        backgroundDimmer = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(resources.getColor(R.color.black_dim, theme))
            visibility = View.GONE
        }
        rootView.addView(backgroundDimmer)

        // 클릭 리스너 설정
        tvEdit.setOnClickListener {
            Toast.makeText(this, "프로필 수정 클릭", Toast.LENGTH_SHORT).show()
        }

        btnPayment.setOnClickListener {
            Toast.makeText(this, "요금제 정보 클릭", Toast.LENGTH_SHORT).show()
        }

        tvWithdrawal.setOnClickListener {
            // 회원 탈퇴 기능 처리: 팝업창 띄우기
            showWithdrawalDialog()
        }

        // 예시: 사용자 데이터를 동적으로 업데이트
        updateUserData()
    }

    private fun updateUserData() {
        val nickname = "미라클이노베이팅하는사람"
        val email = "example@test.com"
        val subscriptionType = "Pro"
        val subscriptionDate = "2024.07.12 갱신됨"

        findViewById<TextView>(R.id.tvNickname).text = nickname
        findViewById<TextView>(R.id.tvEmail).text = email
        findViewById<TextView>(R.id.tvSubscriptionType).text = subscriptionType
        findViewById<TextView>(R.id.tvSubscriptionDate).text = subscriptionDate
    }

    private fun showWithdrawalDialog() {
        val builder = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.dialog_confirm_withdrawal, null)
        builder.setView(customLayout)
        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancelDialog = customLayout.findViewById<Button>(R.id.btnCancelDialog)
        val btnWithdrawalDialog = customLayout.findViewById<Button>(R.id.btnWithdrawalDialog)

        btnCancelDialog.setOnClickListener {
            alertDialog?.dismiss()
            backgroundDimmer.visibility = View.GONE
        }

        btnWithdrawalDialog.setOnClickListener {
            alertDialog?.dismiss()
            backgroundDimmer.visibility = View.GONE
            // 실제 탈퇴 처리 로직
            Toast.makeText(this, "탈퇴 처리가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        backgroundDimmer.visibility = View.VISIBLE
        alertDialog?.show()
    }
}