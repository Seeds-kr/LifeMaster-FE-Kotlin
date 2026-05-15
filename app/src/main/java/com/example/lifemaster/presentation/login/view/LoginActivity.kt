package com.example.lifemaster.presentation.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.databinding.ActivityLoginBinding
import com.example.lifemaster.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.NavHostFragment
import com.example.lifemaster.R

import com.example.lifemaster.network.TokenManager
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject lateinit var tokenManager: TokenManager
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 상태 유지 확인: 저장된 토큰이 있고, 딥링크(콜백)로 들어온 것이 아닐 때 메인으로 이동
        if (!tokenManager.accessToken.isNullOrBlank() && intent?.data == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (t: Throwable) {
            Log.e("LOGIN_ACTIVITY_STARTUP_CRASH", "LoginActivity inflate/setContentView failed", t)
            Toast.makeText(this, "초기화 오류: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        handlePasswordResetDeepLink(intent)
    }

    private fun handlePasswordResetDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val token = data.getQueryParameter("token") ?: return

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment
                ?: return
        val navController = navHostFragment.navController

        if (navController.currentDestination?.id == R.id.resetPasswordFragment) return

        val args = Bundle().apply { putString("token", token) }
        navController.navigate(R.id.resetPasswordFragment, args)
    }
}