package com.example.lifemaster.presentation.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.databinding.ActivityLoginBinding
import com.example.lifemaster.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (t: Throwable) {
            Log.e("LOGIN_ACTIVITY_STARTUP_CRASH", "LoginActivity inflate/setContentView failed", t)
            Toast.makeText(this, "초기화 오류: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        handleNaverCallback(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNaverCallback(intent)
    }

    /**
     * 백엔드 /naverLogin/callback 처리 후 lifemaster://naver/callback?token=xxx 로
     * 리다이렉트했을 때 호출됩니다. 토큰을 꺼내 메인으로 이동합니다.
     */
    private fun handleNaverCallback(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme != "lifemaster" || data.host != "naver" || data.pathSegments.firstOrNull() != "callback") return
        val token = data.getQueryParameter("token")
        if (!token.isNullOrBlank()) {
            Toast.makeText(this, "네이버 로그인에 성공했습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("user_token", token)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}