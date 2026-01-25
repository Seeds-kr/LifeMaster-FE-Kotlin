// presentation/login/view/FindPasswordFragment.kt
package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentFindPasswordBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.login.model.EmailRequest
import com.example.lifemaster.presentation.login.model.PasswordResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindPasswordFragment : Fragment(R.layout.fragment_find_password) {

    private lateinit var binding: FragmentFindPasswordBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFindPasswordBinding.bind(view)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        btnSignup.setOnClickListener {
            val email = editEmail.text.toString().trim()

            if (email.isEmpty()) {
                toast("이메일을 입력해주세요.")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("올바른 이메일 형식으로 입력해주세요.")
                return@setOnClickListener
            }

            // 스웨거와 동일한 형태의 JSON 객체: { "email": "..." }
            val body = EmailRequest(email)

            btnSignup.isEnabled = false

            RetrofitInstance.networkService
                .requestResetEmail(body)
                .enqueue(object : Callback<PasswordResponseDto> {

                    override fun onResponse(
                        call: Call<PasswordResponseDto>,
                        response: Response<PasswordResponseDto>
                    ) {
                        btnSignup.isEnabled = true
                        val ok = response.isSuccessful && response.body()?.success == true
                        if (ok) {
                            toast("인증 메일이 전송되었습니다. 메일함을 확인해주세요.")
                        } else {
                            toast(
                                "요청 실패: code=${response.code()} / " +
                                        (response.body()?.message ?: response.message())
                            )
                        }
                    }

                    override fun onFailure(call: Call<PasswordResponseDto>, t: Throwable) {
                        btnSignup.isEnabled = true
                        toast("네트워크 오류: ${t.localizedMessage}")
                    }
                })
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
