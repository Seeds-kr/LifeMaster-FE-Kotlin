package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    private lateinit var binding: FragmentResetPasswordBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        btnPasswordChange.setOnClickListener {
            val newPassword = editNewPassword.text.toString()
            val passwordCheck = editPasswordCheck.text.toString()
            val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}\$".toRegex()

            if (!newPassword.matches(passwordRegex)) {
                tvNewPasswordError.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                tvNewPasswordError.visibility = View.GONE
            }

            if (newPassword != passwordCheck) {
                tvPasswordCheckError.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                tvPasswordCheckError.visibility = View.GONE
            }

            // 임시 성공 처리
            Toast.makeText(requireContext(), "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()

            // API 연동 후 로그인 화면으로 이동
            // findNavController().navigate(R.id.loginEmailFragment)
        }
    }
}
