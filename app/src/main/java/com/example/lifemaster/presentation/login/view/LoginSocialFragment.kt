package com.example.lifemaster.presentation.login.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentLoginSocialBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.MainActivity
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
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
        // 이메일로 로그인, 이메일로 가입
        btnEmailLogin.setOnClickListener {
            findNavController().navigate(R.id.loginEmailFragment)
        }

        btnEmailSignup.setOnClickListener {
            findNavController().navigate(R.id.signupEmailFragment)
        }

        btnNaverLogin.setOnClickListener {
            loginWithNaver()
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

    private fun loginWithNaver() {
        RetrofitInstance.networkService.getNaverAuthUrl().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val authUrl = response.body()?.trim()
                    if (!authUrl.isNullOrBlank()) {
                        // 네이버 인증 URL을 브라우저로 열기 (nid.naver.com/oauth2.0/authorize?...)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                        startActivity(intent)
                        Log.d("NaverLogin", "네이버 로그인 URL 열기: $authUrl")
                    } else {
                        Toast.makeText(requireContext(), "인증 URL을 받아오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("NaverLogin", "authUrl 요청 실패: ${response.code()}")
                    Toast.makeText(requireContext(), "네이버 로그인 URL을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("NaverLogin", "authUrl 요청 에러", t)
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}