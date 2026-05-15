package com.example.lifemaster.presentation.group.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
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
        val tvCurrent = view.findViewById<TextView>(R.id.tv_page_current)
        val tvTotal = view.findViewById<TextView>(R.id.tv_page_total)

        adapter = GroupListAdapter { g ->
            val b = Bundle().apply {
                putLong("groupId", g.id)
                putString("groupName", g.name)
                putInt("memberCount", g.memberCount ?: 0)
            }
            findNavController().navigate(R.id.action_groupListFragment_to_groupStatsFragment, b)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadAllGroups(tvCurrent, tvTotal)

        etSearch.doAfterTextChanged { editable ->
            val q = editable?.toString()?.trim().orEmpty()
            val filtered =
                if (q.isBlank()) allGroups
                else allGroups.filter { it.name.contains(q, ignoreCase = true) }

            adapter.submitList(filtered)
            tvCurrent.text = "1"
            tvTotal.text = "1"
        }
    }

    private fun loadAllGroups(tvCurrent: TextView, tvTotal: TextView) {
        val token = TokenProvider.getBearerToken(requireContext())
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            runCatching {
                networkService.getAllGroups(token)
            }.onSuccess { list ->
                allGroups = list.distinctBy { it.id }
                adapter.submitList(allGroups)
                tvCurrent.text = "1"
                tvTotal.text = "1"
            }.onFailure { e ->
                Toast.makeText(requireContext(), "전체 그룹 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}