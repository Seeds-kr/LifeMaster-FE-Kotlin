package com.example.lifemaster.presentation.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityLoginBinding
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnKakaoLogin.setOnClickListener {
            loginWithKaKao(
                context = this,
                onSuccess = { token ->
                    Log.d("kakao login token : ", token)
                    Toast.makeText(this, "로그인에 성공하였습니다!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                },
                onFailure = { error ->
                    Log.d("kakao login error : ", error.toString())
                    Toast.makeText(this, "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    fun loginWithKaKao(
        context: Context,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if(error != null) {
                onFailure(error)
            } else if(token != null) {
                onSuccess(token.accessToken)
            }
        }
    }
}