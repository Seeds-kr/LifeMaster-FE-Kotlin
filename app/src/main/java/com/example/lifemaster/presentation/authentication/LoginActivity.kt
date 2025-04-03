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
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.authentication.model.LoginInfo
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private fun setupListeners() = with(binding) {
        btnKakaoLogin.setOnClickListener {
            loginWithKaKao(
                context = this@LoginActivity,
                onSuccess = { token ->
                    Log.d("kakao login token : ", token)
                    Toast.makeText(this@LoginActivity, "로그인에 성공하였습니다!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                },
                onFailure = { error ->
                    Log.d("kakao login error : ", error.toString())
                    Toast.makeText(this@LoginActivity, "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()
                }
            )
        }
        btnGeneralLogin.setOnClickListener {
            RetrofitInstance.networkService.enterUserLogin(loginInfo = LoginInfo(email = "seyeongb18@gmail.com", password = "0324")).enqueue(object: Callback<String> {
                override fun onResponse(
                    call: Call<String?>,
                    response: Response<String?>
                ) {
                    if(response.isSuccessful) {
                        val token = response.body()
                        Toast.makeText(this@LoginActivity, "로그인이 성공했습니다!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("user_token", token)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<String?>, t: Throwable) {
                    Log.d("server error: ", t.message!!)
                }

            })
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