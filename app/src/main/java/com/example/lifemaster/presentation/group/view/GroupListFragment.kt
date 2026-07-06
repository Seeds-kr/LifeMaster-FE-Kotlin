package com.example.lifemaster.presentation.group.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.group.adapter.GroupListAdapter
import com.example.lifemaster.presentation.group.model.GroupResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GroupListFragment : Fragment(R.layout.fragment_group_list) {

    private lateinit var adapter: GroupListAdapter
    private var allGroups: List<GroupResponse> = emptyList()

    @Inject
    lateinit var networkService: NetworkService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_group_list)
        val etSearch = view.findViewById<EditText>(R.id.search_group)

        adapter = GroupListAdapter { group ->
            val bundle = Bundle().apply {
                putLong("groupId", group.id)
                putString("groupName", group.name)
                putInt("memberCount", group.memberCount ?: 0)
                putString("groupAccessType", group.accessType.orEmpty())
            }

            findNavController().navigate(
                R.id.action_groupListFragment_to_groupStatsFragment,
                bundle
            )
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadAllGroups()

        etSearch.doAfterTextChanged { editable ->
            val query = editable?.toString()?.trim().orEmpty()

            val filteredGroups = if (query.isBlank()) {
                allGroups
            } else {
                allGroups.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }

            adapter.submitList(filteredGroups)
        }
    }

    private fun loadAllGroups() {
        val token = TokenProvider.getBearerToken(requireContext())

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            runCatching {
                networkService.getAllGroups(token)
            }.onSuccess { groups ->
                allGroups = groups.distinctBy { it.id }
                adapter.submitList(allGroups)
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    "전체 그룹 목록을 불러오지 못했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}