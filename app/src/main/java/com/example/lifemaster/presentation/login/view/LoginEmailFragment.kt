package com.example.lifemaster.presentation.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentLoginEmailBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.presentation.login.model.LoginInfo
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class LoginEmailFragment : Fragment(R.layout.fragment_login_email) {

    @Inject
    lateinit var networkService: NetworkService

    private var _binding: FragmentLoginEmailBinding? = null
    private val binding get() = _binding!!

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
        networkService.enterUserLogin(LoginInfo(email, password))
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, res: Response<String>) {
                    if (!res.isSuccessful) {
                        toast(parseErrorMessage(res))
                        return
                    }
                    val tokenOrId = res.body().orEmpty()
                    saveToken(tokenOrId)

                    val userId = extractUserId(tokenOrId)
                    if (userId == null || userId <= 0L) {
                        toast("로그인 성공했지만 사용자 ID를 확인할 수 없어요.")
                    }

                    startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
                        putExtra("user_token", tokenOrId)
                    })
                    requireActivity().finish()
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    toast("네트워크 오류가 발생했어요. 잠시 후 다시 시도해 주세요.")
                }
            })
    }

    private fun parseErrorMessage(res: Response<*>): String {
        val defaultMsg = "아이디 또는 비밀번호가 잘못되었습니다."

        if (res.code() in listOf(400, 401, 403, 404)) return defaultMsg

        val raw = try { res.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }

        if (raw.contains("credential", true) ||
            raw.contains("password", true) ||
            raw.contains("email", true) ||
            raw.contains("user not", true) ||
            raw.contains("not found", true)
        ) return defaultMsg

        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(raw)?.groupValues?.getOrNull(1)
            ?: "로그인에 실패했어요 (${res.code()})"
    }

    private fun saveToken(token: String) {
        requireContext()
            .getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit { putString("token", token) }
    }

    private fun extractUserId(tokenOrId: String): Long? {
        tokenOrId.toLongOrNull()?.let { return it }
        return try {
            val parts = tokenOrId.split(".")
            if (parts.size >= 2) {
                val json = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
                val obj = JSONObject(json)
                when {
                    obj.has("id") -> obj.getLong("id")
                    obj.has("userId") -> obj.getLong("userId")
                    obj.has("sub") -> obj.getLong("sub")
                    else -> null
                }
            } else null
        } catch (_: Exception) {
            Regex("(\\d+)").find(tokenOrId)?.groupValues?.getOrNull(1)?.toLongOrNull()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}