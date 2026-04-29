package com.example.lifemaster.presentation.total

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentTotalBinding
import com.example.lifemaster.presentation.total.mypage.view.MyPageActivity

class TotalFragment : Fragment(R.layout.fragment_total) {

    private lateinit var binding: FragmentTotalBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTotalBinding.bind(view)
        initListeners()
    }

    private fun initListeners() {
        binding.cvDetox.setOnClickListener {
            findNavController().navigate(R.id.action_totalFragment_to_detoxFragment)
        }

        binding.cvIntrospection.setOnClickListener {
            findNavController().navigate(R.id.action_totalFragment_to_introspectionFragment)
        }

        binding.cvChallenge.setOnClickListener {
            findNavController().navigate(R.id.action_totalFragment_to_challengeFragment)
        }

        binding.cvMypage.setOnClickListener {
            // MyPage는 Navigation Graph의 Fragment가 아니라 Activity로 구현되어 있습니다.
            startActivity(Intent(requireContext(), MyPageActivity::class.java))
        }
    }
}