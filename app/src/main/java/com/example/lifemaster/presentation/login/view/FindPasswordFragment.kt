package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentFindPasswordBinding
import androidx.navigation.fragment.findNavController

class FindPasswordFragment : Fragment(R.layout.fragment_find_password) {

    private lateinit var binding: FragmentFindPasswordBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFindPasswordBinding.bind(view)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        btnSignup.setOnClickListener {
            val email = editEmail.text.toString()
            if (email.isNotEmpty()) {
                Toast.makeText(requireContext(), "인증 메일이 전송되었습니다.", Toast.LENGTH_SHORT).show()

                // 임시로 바로 비밀번호 재설정 화면으로 이동
                findNavController().navigate(R.id.resetPasswordFragment)

                // TODO: 나중에 실제 이메일 인증 API 연동 후 성공 시에만 이동
            } else {
                Toast.makeText(requireContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
