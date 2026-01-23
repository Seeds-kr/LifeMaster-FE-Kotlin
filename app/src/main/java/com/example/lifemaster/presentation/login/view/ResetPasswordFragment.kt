package com.example.lifemaster.presentation.login.view

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentResetPasswordBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.login.model.PasswordResetDto
import com.example.lifemaster.presentation.login.model.PasswordResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    private lateinit var binding: FragmentResetPasswordBinding
    private var token: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 토큰 추출(임시)
        token = arguments?.getString("token") ?: extractTokenFromIntent()
        if (token.isNullOrBlank()) {
            toast("유효하지 않은 접근입니다. 메일 링크를 통해 다시 시도해 주세요.")
            return
        }

        // 진입 시 토큰 검증
        verifyToken(token!!)

        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        btnPasswordChange.setOnClickListener {
            val newPw = editNewPassword.text.toString()
            val check = editPasswordCheck.text.toString()
            val rule = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}\$".toRegex()

            if (!newPw.matches(rule)) {
                tvNewPasswordError.visibility = View.VISIBLE
                return@setOnClickListener
            } else tvNewPasswordError.visibility = View.GONE

            if (newPw != check) {
                tvPasswordCheckError.visibility = View.VISIBLE
                return@setOnClickListener
            } else tvPasswordCheckError.visibility = View.GONE

            val tk = token
            if (tk.isNullOrBlank()) { toast("토큰이 없습니다. 메일 링크로 다시 들어오세요."); return@setOnClickListener }

            btnPasswordChange.isEnabled = false

            RetrofitInstance.networkService
                .resetPassword(PasswordResetDto(token = tk, newPassword = newPw, checkPassword = check))
                .enqueue(object : Callback<PasswordResponseDto> {
                    override fun onResponse(call: Call<PasswordResponseDto>, res: Response<PasswordResponseDto>) {
                        btnPasswordChange.isEnabled = true
                        val body = res.body()
                        if (res.isSuccessful && body?.success == true) {
                            toast("비밀번호가 변경되었습니다.")
                            // 필요 시 로그인 화면으로 이동
                        } else {
                            toast("변경 실패: code=${res.code()} / ${body?.message ?: res.message()}")
                        }
                    }
                    override fun onFailure(call: Call<PasswordResponseDto>, t: Throwable) {
                        btnPasswordChange.isEnabled = true
                        toast("네트워크 오류: ${t.localizedMessage}")
                    }
                })
        }
    }

    private fun verifyToken(token: String) {
        RetrofitInstance.networkService
            .verifyResetToken(token)
            .enqueue(object : Callback<PasswordResponseDto> {
                override fun onResponse(call: Call<PasswordResponseDto>, res: Response<PasswordResponseDto>) {
                    val ok = res.isSuccessful && (res.body()?.success == true)
                    if (!ok) toast("토큰 검증 실패: 다시 메일 링크로 시도해 주세요.")
                }
                override fun onFailure(call: Call<PasswordResponseDto>, t: Throwable) {
                    toast("토큰 검증 네트워크 오류: ${t.localizedMessage}")
                }
            })
    }

    private fun extractTokenFromIntent(): String? {
        val data: Uri? = activity?.intent?.data
        return data?.getQueryParameter("token")
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
