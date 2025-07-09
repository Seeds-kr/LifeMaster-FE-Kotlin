package com.example.lifemaster.presentation.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentLoginSocialBinding
import com.example.lifemaster.presentation.MainActivity
import com.kakao.sdk.user.UserApiClient

class LoginSocialFragment: Fragment(R.layout.fragment_login_social) {

    lateinit var binding: FragmentLoginSocialBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginSocialBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        btnKakaoLogin.setOnClickListener {
            loginWithKaKao(
                context = requireContext(),
                onSuccess = { token ->
                    Log.d("kakao login token : ", token)
                    Toast.makeText(requireContext(), "로그인에 성공하였습니다!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                },
                onFailure = { error ->
                    Log.d("kakao login error : ", error.toString())
                    Toast.makeText(requireContext(), "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()
                }
            )
        }
        btnGeneralLogin.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                putExtra("user_token", "1234")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
//            RetrofitInstance.networkService.enterUserLogin(loginInfo = LoginInfo(email = "seyeongb18@gmail.com", password = "0324")).enqueue(object: Callback<String> {
//                override fun onResponse(
//                    call: Call<String?>,
//                    response: Response<String?>
//                ) {
//                    if(response.isSuccessful) {
//                        val token = response.body()
//                        Toast.makeText(requireContext(), "로그인이 성공했습니다!", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(requireContext(), MainActivity::class.java).apply {
//                            putExtra("user_token", token)
//                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        }
//                        startActivity(intent)
//                    }
//                }
//
//                override fun onFailure(call: Call<String?>, t: Throwable) {
//                    Log.d("server error: ", t.message!!)
//                }
//
//            })
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