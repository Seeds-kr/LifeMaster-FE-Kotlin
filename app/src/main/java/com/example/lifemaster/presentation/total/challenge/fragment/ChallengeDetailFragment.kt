package com.example.lifemaster.presentation.total.challenge.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentChallengeDetailBinding

class ChallengeDetailFragment : Fragment(R.layout.fragment_challenge_detail) {
    private lateinit var binding: FragmentChallengeDetailBinding

    private val args: ChallengeDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChallengeDetailBinding.bind(view)

        val challengeId = args.challId
        Log.d("ChallengeDetail", "전달받은 챌린지 ID: $challengeId")

        initListeners()
    }

    private fun initListeners() {
        // '참여하기' 버튼(ID: btn_join)에 클릭 리스너 설정
        binding.btnJoin.setOnClickListener {
            Toast.makeText(requireContext(), "챌린지 참여 완료!", Toast.LENGTH_SHORT).show()
        }
    }
}