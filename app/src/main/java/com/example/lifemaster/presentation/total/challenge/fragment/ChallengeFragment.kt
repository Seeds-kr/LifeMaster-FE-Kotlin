// 1. 패키지 이름을 fragment에 맞게 수정합니다.
package com.example.lifemaster.presentation.total.challenge.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.databinding.FragmentChallengeBinding
import com.example.lifemaster.presentation.total.challenge.fragment.adapter.ChallengeAdapter

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()

        // TODO: ViewModel과 연결하여 Paging 데이터를 구독하고 어댑터에 제출해야 합니다.
        // 예시:
        // viewModel.challengePagingData.observe(viewLifecycleOwner) { pagingData ->
        //     challengeAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
        // }
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter()

        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        challengeAdapter.onItemClickListener = { challenge ->
            Toast.makeText(context, "${challenge.challName} 클릭됨", Toast.LENGTH_SHORT).show()
        }

        challengeAdapter.onJoinButtonClickListener = { challenge ->
            Toast.makeText(context, "${challenge.challName} 참여 버튼 클릭됨", Toast.LENGTH_SHORT).show()
            // TODO: ViewModel을 통해 참여 API를 호출하는 로직 구현
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
