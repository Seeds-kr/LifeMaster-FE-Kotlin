package com.example.lifemaster.presentation.login.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentSignupProfileBinding
import com.example.lifemaster.presentation.MainActivity

class SignupProfileFragment : Fragment(R.layout.fragment_signup_profile) {

    private lateinit var binding: FragmentSignupProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignupProfileBinding.bind(view)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupNicknameChecker()
        setupProfileImagePicker()

        binding.btnStart.setOnClickListener {
            if (binding.btnStart.isEnabled) {
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra("user_token", "sample_token")
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun setupNicknameChecker() = with(binding) {
        editNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nickname = s.toString().trim()
                if (nickname.isNotEmpty()) {
                    layoutNicknameStatus.visibility = View.VISIBLE
                    btnStart.isEnabled = true
                    btnStart.text = "라이프마스터 시작하기"
                    btnStart.setBackgroundResource(R.drawable.bg_round_and_mint)
                } else {
                    layoutNicknameStatus.visibility = View.INVISIBLE
                    btnStart.isEnabled = false
                    btnStart.text = "닉네임을 입력해주세요"
                    btnStart.setBackgroundResource(R.drawable.bg_round_and_mint_60)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupProfileImagePicker() {
        binding.frameProfile.clipToOutline = true

        binding.frameProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                binding.ivProfile.setImageURI(it)
                binding.ivDefaultIcon.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 100
    }
}