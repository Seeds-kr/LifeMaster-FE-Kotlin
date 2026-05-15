package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentResetPasswordBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.login.model.PasswordResetDto
import com.example.lifemaster.presentation.login.model.PasswordResponseDto
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    private lateinit var binding: FragmentResetPasswordBinding
    private var token: String? = null

    @Inject lateinit var networkService: NetworkService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        token = arguments?.getString("token")
        if (token.isNullOrBlank()) {
            toast("유효하지 않은 접근입니다. 비밀번호 찾기를 다시 진행해주세요.")
            return
        }

        setupRealtimeValidation()
        setupListeners()
    }

    private fun setupRealtimeValidation() = with(binding) {

        fun updateCheckMismatch() {
            val pw = editNewPassword.text.toString()
            val check = editPasswordCheck.text.toString()

            if (check.isEmpty()) {
                tvPasswordCheckError.visibility = View.GONE
                return
            }

            tvPasswordCheckError.visibility =
                if (pw == check) View.GONE else View.VISIBLE
        }

        editPasswordCheck.doAfterTextChanged { updateCheckMismatch() }
        editNewPassword.doAfterTextChanged { updateCheckMismatch() }
    }

    private fun setupListeners() = with(binding) {
        btnPasswordChange.setOnClickListener {
            val newPw = editNewPassword.text.toString()
            val check = editPasswordCheck.text.toString()

            // 영문 + 숫자 + 특수문자(@$!%*#?&) 각 1개 이상, 8~16자
            val rule =
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,16}$"
                    .toRegex()

            // 비밀번호 규칙 검사
            if (!newPw.matches(rule)) {
                tvNewPasswordError.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                tvNewPasswordError.visibility = View.GONE
            }

            // 비밀번호 일치 검사
            if (newPw != check) {
                tvPasswordCheckError.visibility = View.VISIBLE
                toast("비밀번호가 일치하지 않아요")
                return@setOnClickListener
            } else {
                tvPasswordCheckError.visibility = View.GONE
            }

            val tk = token!!
            btnPasswordChange.isEnabled = false

            networkService
                .resetPassword(
                    PasswordResetDto(
                        token = tk,
                        newPassword = newPw,
                        checkPassword = check
                    )
                )
                .enqueue(object : Callback<PasswordResponseDto> {

                    override fun onResponse(
                        call: Call<PasswordResponseDto>,
                        res: Response<PasswordResponseDto>
                    ) {
                        btnPasswordChange.isEnabled = true
                        val body = res.body()

                        if (res.isSuccessful && body?.success == true) {
                            toast("비밀번호가 변경되었습니다")

                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph_login, true) // 그래프까지 싹 비움
                                .build()

                            findNavController().navigate(
                                R.id.loginEmailFragment,
                                null,
                                navOptions
                            )
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

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}