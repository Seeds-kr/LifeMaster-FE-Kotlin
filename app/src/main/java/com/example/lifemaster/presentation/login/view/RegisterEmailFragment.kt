package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentRegisterEmailBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.login.model.RegResponse
import com.example.lifemaster.presentation.login.model.RegisterInfo
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class RegisterEmailFragment : Fragment(R.layout.fragment_register_email) {

    private var _binding: FragmentRegisterEmailBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var networkService: NetworkService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSignup.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val pw = binding.editPassword.text.toString()
            val pwc = binding.editPasswordConfirm.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("올바른 이메일 형식으로 입력해주세요"); return@setOnClickListener
            }
            if (pw.isEmpty() || pwc.isEmpty()) {
                toast("비밀번호를 입력해주세요"); return@setOnClickListener
            }
            if (pw != pwc) {
                toast("비밀번호가 일치하지 않아요"); return@setOnClickListener
            }
            val pwRegex = "^(?=.*[A-Za-z])(?=.*\\d).{8,16}$".toRegex()
            if (!pw.matches(pwRegex)) {
                toast("비밀번호는 대소문자, 숫자를 포함해 8~16자여야 해요"); return@setOnClickListener
            }

            binding.btnSignup.isEnabled = false

            networkService.registerUser(RegisterInfo(email, pw, pwc))
                .enqueue(object : Callback<RegResponse> {
                    override fun onResponse(call: Call<RegResponse>, res: Response<RegResponse>) {
                        binding.btnSignup.isEnabled = true
                        if (!res.isSuccessful) {
                            toast(parseErrorMessage(res))
                            return
                        }
                        val regId = res.body()?.regId.orEmpty()
                        if (regId.isBlank()) {
                            toast("regId를 받지 못했습니다"); return
                        }

                        findNavController().navigate(
                            R.id.signupProfileFragment,
                            Bundle().apply {
                                putString("regId", regId)
                                putString("email", email)
                                putString("password", pw)
                            }
                        )
                    }

                    override fun onFailure(call: Call<RegResponse>, t: Throwable) {
                        binding.btnSignup.isEnabled = true
                        toast("네트워크 오류: ${t.message}")
                    }
                })
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.loginEmailFragment)
        }
    }

    private fun parseErrorMessage(res: Response<*>): String {
        return try {
            val raw = res.errorBody()?.string().orEmpty()
            Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
                .find(raw)
                ?.groupValues?.getOrNull(1)
                ?: "회원가입 실패 (${res.code()})"
        } catch (_: Exception) {
            "회원가입 실패 (${res.code()})"
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}