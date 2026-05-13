package com.example.lifemaster.presentation.group.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class GroupFragment : Fragment(R.layout.fragment_group) {

    @Inject
    lateinit var networkService: NetworkService

    private lateinit var joinedAdapter: GroupListAdapter
    private lateinit var searchAdapter: GroupListAdapter

    private var joinedAll: List<GroupResponse> = emptyList()
    private var allGroups: List<GroupResponse> = emptyList()
    private var joinedGroupIds: Set<Long> = emptySet()

    private var expanded = false
    private val collapsedShowCount = 3

    private var searchPopup: PopupWindow? = null

    private lateinit var rvJoined: RecyclerView
    private lateinit var btnMore: LinearLayout
    private lateinit var tvMore: TextView
    private lateinit var ivMore: ImageView
    private lateinit var tvJoinedCount: TextView
    private lateinit var searchContainer: View
    private lateinit var etSearchInput: EditText
    private lateinit var btnCreateGroup: View

    private var isPremiumLocked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btn_all_groups_list)?.setOnClickListener {
            findNavController().navigate(R.id.action_groupFragment_to_groupListFragment)
        }

        btnCreateGroup = view.findViewById(R.id.create_group)
        btnCreateGroup.setOnClickListener {
            if (isPremiumLocked) {
                Toast.makeText(
                    requireContext(),
                    "프리미엄 구독 후 이용할 수 있어요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            findNavController().navigate(R.id.action_groupFragment_to_groupCreateFragment)
        }

        val joinedRoot = view.findViewById<View>(R.id.include_joined_groups_list)
        rvJoined = joinedRoot.findViewById(R.id.rv_group_list)
        btnMore = joinedRoot.findViewById(R.id.btn_joined_groups_more)
        tvMore = joinedRoot.findViewById(R.id.tv_more)
        ivMore = joinedRoot.findViewById(R.id.iv_more_down)
        tvJoinedCount = view.findViewById(R.id.tv_joined_groups_count)

        joinedAdapter = GroupListAdapter { group ->
            openGroupStats(group)
        }

        searchAdapter = GroupListAdapter { group ->
            dismissSearchPopup()
            openGroupStats(group)
        }

        rvJoined.layoutManager = LinearLayoutManager(requireContext())
        rvJoined.adapter = joinedAdapter

        btnMore.setOnClickListener {
            expanded = !expanded
            applyJoinedListUi()
        }

        searchContainer = view.findViewById(R.id.et_group_search)
        etSearchInput = view.findViewById(R.id.search_group)

        val openDropdown: (View) -> Unit = {
            ensureAllGroupsLoadedThenShow(
                anchor = etSearchInput,
                initialQuery = etSearchInput.text?.toString().orEmpty()
            )
        }

        searchContainer.setOnClickListener(openDropdown)
        etSearchInput.setOnClickListener(openDropdown)

        etSearchInput.doAfterTextChanged { text ->
            if (searchPopup?.isShowing == true) {
                submitFilteredGroups(text?.toString().orEmpty())
            }
        }

        clearJoinedGroupsUi()
    }

    override fun onResume() {
        super.onResume()
        dismissSearchPopup()
        etSearchInput.setText("")
        clearJoinedGroupsUi()
        loadGroupData()
    }

    private fun openGroupStats(group: GroupResponse) {
        val bundle = Bundle().apply {
            putLong("groupId", group.id)
            putString("groupName", group.name)
            putInt("memberCount", group.memberCount ?: 0)
        }

        findNavController().navigate(
            R.id.action_groupFragment_to_groupStatsFragment,
            bundle
        )
    }

    private fun clearJoinedGroupsUi() {
        joinedAll = emptyList()
        allGroups = emptyList()
        joinedGroupIds = emptySet()
        expanded = false

        tvJoinedCount.text = "0"
        joinedAdapter.submitList(emptyList())
        searchAdapter.submitList(emptyList())

        val layoutParams = rvJoined.layoutParams
        layoutParams.height = 0
        rvJoined.layoutParams = layoutParams

        tvMore.text = getString(R.string.more)
        ivMore.setImageResource(R.drawable.ic_arrow_down)
        btnMore.visibility = View.GONE
    }

    private fun loadGroupData() {
        val token = TokenProvider.getBearerToken(requireContext())

        if (token.isNullOrBlank()) {
            clearJoinedGroupsUi()
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val myGroupsDeferred = async {
                runCatching {
                    networkService.getMyGroups(token)
                }
            }

            val allGroupsDeferred = async {
                runCatching {
                    networkService.getAllGroups(token)
                }
            }

            val myGroupsResult = myGroupsDeferred.await()
            val allGroupsResult = allGroupsDeferred.await()

            val myGroupsError = myGroupsResult.exceptionOrNull()
            isPremiumLocked = isPremiumError(myGroupsError)

            val myGroups = if (isPremiumLocked) {
                emptyList()
            } else {
                myGroupsResult.getOrElse { emptyList() }
            }.distinctBy { it.id }

            val all = allGroupsResult.getOrElse { error ->
                Toast.makeText(
                    requireContext(),
                    "그룹 목록 조회 실패: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                emptyList()
            }.distinctBy { it.id }

            joinedAll = myGroups
            joinedGroupIds = joinedAll.map { it.id }.toSet()
            allGroups = all

            tvJoinedCount.text = joinedAll.size.toString()
            expanded = false
            applyJoinedListUi()

            btnMore.visibility =
                if (joinedAll.size > collapsedShowCount) View.VISIBLE else View.GONE
        }
    }

    private fun isPremiumError(error: Throwable?): Boolean {
        return error is HttpException && error.code() == 403
    }

    private fun applyJoinedListUi() {
        val showList = if (expanded) joinedAll else joinedAll.take(collapsedShowCount)
        joinedAdapter.submitList(showList.toList())

        val layoutParams = rvJoined.layoutParams
        layoutParams.height = when {
            showList.isEmpty() -> 0
            expanded -> LinearLayout.LayoutParams.WRAP_CONTENT
            else -> dpToPx(168)
        }
        rvJoined.layoutParams = layoutParams

        tvMore.text = if (expanded) "접기" else getString(R.string.more)
        ivMore.setImageResource(
            if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
        )
    }

    private fun ensureAllGroupsLoadedThenShow(anchor: View, initialQuery: String) {
        if (allGroups.isEmpty()) {
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
                    showSearchPopup(anchor, initialQuery)
                }.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "그룹 목록 조회 실패: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return
        }

        showSearchPopup(anchor, initialQuery)
    }

    private fun showSearchPopup(anchor: View, initialQuery: String) {
        dismissSearchPopup()

        val content = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_group_search_list, null, false)

        val rv = content.findViewById<RecyclerView>(R.id.rv_group_search_list)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = searchAdapter
        rv.setHasFixedSize(true)

        val popup = PopupWindow(content, anchor.width, dpToPx(240), true).apply {
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = dpToPx(14).toFloat()
            setOnDismissListener { searchPopup = null }
        }

        searchPopup = popup
        popup.showAsDropDown(anchor, -dpToPx(6), 0)

        submitFilteredGroups(initialQuery)
    }

    private fun submitFilteredGroups(query: String) {
        val trimmedQuery = query.trim()

        val base = allGroups.filterNot {
            joinedGroupIds.contains(it.id)
        }

        val filtered = if (trimmedQuery.isEmpty()) {
            base
        } else {
            base.filter {
                it.name.contains(trimmedQuery, ignoreCase = true)
            }
        }

        searchAdapter.submitList(filtered.toList())
    }

    private fun dismissSearchPopup() {
        searchPopup?.dismiss()
        searchPopup = null
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        dismissSearchPopup()
        super.onDestroyView()
    }
}