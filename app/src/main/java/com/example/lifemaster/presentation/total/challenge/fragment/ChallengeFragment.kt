package com.example.lifemaster.presentation.total.challenge.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentChallengeBinding
import com.example.lifemaster.presentation.total.challenge.fragment.adapter.ChallengeAdapter
import com.example.lifemaster.presentation.total.challenge.viewmodel.ChallengeViewModel
import kotlinx.coroutines.flow.collectLatest // Flow의 데이터를 수집
import kotlinx.coroutines.launch

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
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
        setupSearchView()

        observeChallengeData()
        observeSearchResults()
    }
    
    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter()
        binding.rvChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeChallengeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // collectLatest: viewModel.challenges Flow에서
            // 새로운 PagingData가 발행될 때마다 이전 작업을 취소하고 새 데이터로 블록을 실행
            viewModel.challenges.collectLatest { pagingData ->
                // 검색 모드가 아닐 때 일반 목록을 표시
                if (!viewModel.isSearchMode.value) {
                    challengeAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { searchResults ->
                if (viewModel.isSearchMode.value) {
                    // 검색 결과를 PagingData로 Adapter에 제출
                    challengeAdapter.submitData(searchResults)
                }
            }
        }
    }

    private fun setupClickListeners() {
        challengeAdapter.onItemClickListener = { challenge ->
            val bundle = Bundle().apply {
                putLong("challId", challenge.challId)
            }
            findNavController().navigate(R.id.action_challengeFragment_to_challengeDetailFragment, bundle)
        }

        challengeAdapter.onJoinButtonClickListener = { challenge ->
            val token = readAuthToken()
            if (token == null) {
                Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@onJoinButtonClickListener
            }
            
            viewModel.joinChallenge(
                token = token,
                challId = challenge.challId,
                onSuccess = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                onError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // SearchView 설정, 검색 기능 연결
    private fun setupSearchView() {
        binding.svSearch.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQuery ->
                    if (searchQuery.isNotBlank()) {
                        performSearch(searchQuery)
                    } else {
                        // 검색어가 비어있으면 검색 모드 해제
                        viewModel.clearSearch()
                        // 일반 목록으로 복귀
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.challenges.collectLatest { pagingData ->
                                challengeAdapter.submitData(pagingData)
                            }
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 실시간 검색이 필요하면 여기서 처리
                // 현재는 검색 버튼 클릭 시에만 검색하도록 구현
                if (newText.isNullOrBlank()) {
                    // 검색어가 비어있으면 검색 모드 해제
                    viewModel.clearSearch()
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.challenges.collectLatest { pagingData ->
                            challengeAdapter.submitData(pagingData)
                        }
                    }
                }
                return false
            }
        })

        // SearchView 닫기 버튼 클릭 시 검색 모드 해제
        binding.svSearch.setOnCloseListener {
            viewModel.clearSearch()
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.challenges.collectLatest { pagingData ->
                    challengeAdapter.submitData(pagingData)
                }
            }
            false
        }
    }

    // 검색 수행
    private fun performSearch(query: String) {
        val token = readAuthToken()
        if (token == null) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.searchChallenges(
            token = token,
            searchQuery = query,
            onSuccess = {
                // 검색 결과는 observeSearchResults에서 자동으로 처리됨
            },
            onError = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // SharedPreferences에서 인증 토큰을 읽어옴옴
    // @return Bearer 토큰 문자열 또는 null (로그인하지 않은 경우)
    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
