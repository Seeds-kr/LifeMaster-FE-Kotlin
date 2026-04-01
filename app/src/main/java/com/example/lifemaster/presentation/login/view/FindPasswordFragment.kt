package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentFindPasswordBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.login.model.EmailRequest
import com.example.lifemaster.presentation.login.model.PasswordResponseDto
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class FindPasswordFragment : Fragment(R.layout.fragment_find_password) {

    private lateinit var binding: FragmentFindPasswordBinding

    @Inject lateinit var networkService: NetworkService

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
                toast("이메일을 입력해주세요")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("올바른 이메일 형식으로 입력해주세요")
                return@setOnClickListener
            }

            val body = EmailRequest(email)
            btnSignup.isEnabled = false

            networkService
                .requestResetEmail(body)
                .enqueue(object : Callback<PasswordResponseDto> {

                    override fun onResponse(
                        call: Call<PasswordResponseDto>,
                        response: Response<PasswordResponseDto>
                    ) {
                        btnSignup.isEnabled = true

                        val ok = response.isSuccessful && response.body()?.success == true
                        if (ok) {
                            toast("인증 메일이 전송되었습니다")

                            val args = Bundle().apply { putString("email", email) }
                            findNavController().navigate(
                                R.id.action_findPasswordFragment_to_findPasswordVerificationFragment,
                                args
                            )
                        } else {
                            toast("이메일을 다시 확인해주세요")
                        }
                    }

                    override fun onFailure(call: Call<PasswordResponseDto>, t: Throwable) {
                        btnSignup.isEnabled = true
                        toast("네트워크 오류가 발생했어요")
                    }
                })
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}