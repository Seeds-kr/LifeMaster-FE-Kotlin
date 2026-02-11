package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentFindPasswordVerificationBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.login.model.PasswordResponseDto
import com.example.lifemaster.presentation.login.model.VerifyCodeRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindPasswordVerificationFragment : Fragment(R.layout.fragment_find_password_verification) {

    private lateinit var binding: FragmentFindPasswordVerificationBinding
    private var email: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFindPasswordVerificationBinding.bind(view)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        email = arguments?.getString("email")

        if (email.isNullOrBlank()) {
            toast("이메일 정보가 없습니다. 처음부터 다시 시도해주세요")
            return
        }

        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        btnCheckVerificationCode.setOnClickListener {
            val code = editVerificationCode.text.toString().trim()

            if (code.length != 6) {
                toast("인증번호 6자리를 입력해주세요")
                return@setOnClickListener
            }

            btnCheckVerificationCode.isEnabled = false

            RetrofitInstance.networkService
                .verifyResetCode(VerifyCodeRequest(email = email!!, code = code))
                .enqueue(object : Callback<PasswordResponseDto> {

                    override fun onResponse(
                        call: Call<PasswordResponseDto>,
                        response: Response<PasswordResponseDto>
                    ) {
                        btnCheckVerificationCode.isEnabled = true

                        val body = response.body()
                        val ok = response.isSuccessful &&
                                body?.success == true &&
                                !body.token.isNullOrBlank()

                        if (ok) {
                            val args = Bundle().apply { putString("token", body!!.token) }
                            findNavController().navigate(
                                R.id.action_findPasswordVerificationFragment_to_resetPasswordFragment,
                                args
                            )
                        } else {
                            toast("인증번호를 다시 확인해주세요.")
                            binding.editVerificationCode.setText("")
                            binding.editVerificationCode.requestFocus()
                        }
                    }

                    override fun onFailure(call: Call<PasswordResponseDto>, t: Throwable) {
                        btnCheckVerificationCode.isEnabled = true
                        toast("네트워크 오류: ${t.localizedMessage}")
                    }
                })
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}