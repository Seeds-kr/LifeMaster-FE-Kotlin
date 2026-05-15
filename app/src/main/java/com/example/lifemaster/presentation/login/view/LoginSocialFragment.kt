package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentLoginSocialBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController

import com.example.lifemaster.network.TokenManager
import javax.inject.Inject

@AndroidEntryPoint
class LoginSocialFragment: Fragment(R.layout.fragment_login_social) {

    @Inject lateinit var tokenManager: TokenManager
    lateinit var binding: FragmentLoginSocialBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginSocialBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        // 이메일로 로그인, 이메일로 가입
        btnEmailLogin.setOnClickListener {
            findNavController().navigate(R.id.loginEmailFragment)
        }

        btnEmailSignup.setOnClickListener {
            findNavController().navigate(R.id.signupEmailFragment)
        }
    }
}