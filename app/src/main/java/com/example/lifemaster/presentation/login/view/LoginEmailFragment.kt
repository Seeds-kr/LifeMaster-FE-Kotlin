package com.example.lifemaster.presentation.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentLoginEmailBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenManager
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.presentation.total.mypage.model.MeResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class LoginEmailFragment : Fragment(R.layout.fragment_login_email) {

    private var _binding: FragmentLoginEmailBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var networkService: NetworkService
    @Inject lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnFindPassword.setOnClickListener {
            findNavController().navigate(R.id.findPasswordFragment)
        }

        binding.btnSignup.setOnClickListener {
            findNavController().navigate(R.id.signupEmailFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString()

            when {
                email.isEmpty() || password.isEmpty() ->
                    toast("이메일과 비밀번호를 입력해주세요.")

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    toast("올바른 이메일 형식을 입력해주세요.")

                else -> login(email, password)
            }
        }
    }

    private fun login(email: String, password: String) {
        networkService.enterUserLogin(com.example.lifemaster.presentation.login.model.LoginInfo(email, password))
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, res: Response<String>) {
                    if (!res.isSuccessful) {
                        toast(parseErrorMessage(res))
                        return
                    }

                    val token = res.body()?.trim().orEmpty()
                    if (token.isBlank()) {
                        toast("로그인 토큰이 비어 있어요.")
                        return
                    }

                    // 예전 토큰 흔적 제거 후 새 토큰 저장
                    tokenManager.clear()
                    tokenManager.accessToken = token
                    tokenManager.refreshFromStorage()

                    // 로그인 직후 /users/me 로 실제 사용자 정보 조회
                    lifecycleScope.launch {
                        fetchAndSaveMeThenMove(email, token)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    toast("네트워크 오류가 발생했어요. 잠시 후 다시 시도해 주세요.")
                }
            })
    }

    private suspend fun fetchAndSaveMeThenMove(loginEmail: String, token: String) {
        runCatching {
            networkService.getMe("Bearer $token")
        }.onSuccess { response ->
            if (response.isSuccessful) {
                val me = response.body()
                saveAuthUser(
                    token = token,
                    loginEmail = loginEmail,
                    me = me
                )
            } else {
                // getMe 실패해도 토큰은 저장된 상태이므로 이메일만 저장하고 진입
                saveAuthUser(
                    token = token,
                    loginEmail = loginEmail,
                    me = null
                )
            }

            moveToMain(token)
        }.onFailure {
            // getMe 실패해도 로그인 자체는 성공했으므로 최소 정보 저장 후 진입
            saveAuthUser(
                token = token,
                loginEmail = loginEmail,
                me = null
            )
            moveToMain(token)
        }
    }

    private fun saveAuthUser(
        token: String,
        loginEmail: String,
        me: MeResponse?
    ) {
        // 토큰은 TokenManager/TokenProvider 쪽에서 이미 저장됨
        // 여기서는 부가 정보만 저장
        requireContext()
            .getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit {
                putString("loginEmail", loginEmail)
                putString("email", me?.email ?: loginEmail)

                val nick = (me?.user?.nickName ?: me?.nickName)?.trim().orEmpty()
                if (nick.isNotBlank() && nick != "null") {
                    putString("nickname", nick)
                    putString("nickName", nick)
                } else {
                    remove("nickname")
                    remove("nickName")
                }

                val memberId = me?.id
                if (memberId != null && memberId > 0L) {
                    putLong("memberId", memberId)
                } else {
                    remove("memberId")
                }

                val profileUrl = me?.profileImageUrl?.trim().orEmpty()
                if (profileUrl.isNotBlank()) {
                    putString("profileImageUrl", profileUrl)
                }
            }
    }

    private fun moveToMain(token: String) {
        startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
            putExtra("user_token", token)
        })
        requireActivity().finish()
    }

    private fun parseErrorMessage(res: Response<*>): String {
        val defaultMsg = "아이디 또는 비밀번호가 잘못되었습니다."

        if (res.code() in listOf(400, 401, 403, 404)) return defaultMsg

        val raw = try {
            res.errorBody()?.string().orEmpty()
        } catch (_: Exception) {
            ""
        }

        if (
            raw.contains("credential", true) ||
            raw.contains("password", true) ||
            raw.contains("email", true) ||
            raw.contains("user not", true) ||
            raw.contains("not found", true)
        ) {
            return defaultMsg
        }

        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(raw)
            ?.groupValues
            ?.getOrNull(1)
            ?: "로그인에 실패했어요 (${res.code()})"
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}