package com.example.lifemaster.presentation.group.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lifemaster.R
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.group.model.GroupAchievementHeatmapItem
import com.example.lifemaster.presentation.group.model.GroupGoalProgressResponseItem
import com.example.lifemaster.presentation.group.model.GroupRankingItem
import com.example.lifemaster.presentation.group.model.GroupSleepStatsResponse
import com.example.lifemaster.presentation.group.util.ChartStyle
import com.example.lifemaster.presentation.total.mypage.view.PremiumSubscribeActivity
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

@AndroidEntryPoint
class GroupStatsFragment : Fragment(R.layout.fragment_group_stats) {

    private val args: GroupStatsFragmentArgs by navArgs()

    @Inject
    lateinit var networkService: NetworkService

    private lateinit var tvGroupName: TextView
    private lateinit var tvGroupMemberCount: TextView
    private lateinit var btnJoin: TextView
    private lateinit var btnLeave: TextView
    private lateinit var btnChat: View
    private lateinit var rvRecentAchieve: RecyclerView
    private lateinit var layoutGoalChartContainer: LinearLayout

    private lateinit var layoutRecentAchieveRoot: FrameLayout
    private lateinit var layoutHeatmapTooltip: FrameLayout
    private lateinit var tvTooltipDay: TextView
    private lateinit var tvTooltipCount: TextView
    private lateinit var viewTooltipDot: View

    private lateinit var btnToggleWeek: TextView
    private lateinit var btnToggleAll: TextView
    private lateinit var viewToggleSelected: View

    private lateinit var layoutRankingList: LinearLayout
    private lateinit var layoutMore: LinearLayout
    private lateinit var btnMore: TextView
    private lateinit var ivMoreArrow: ImageView

    private lateinit var tvRecentAchieveTitle: TextView
    private lateinit var layoutRankingHeader: View
    private lateinit var layoutPremiumLocked: View
    private lateinit var btnPremiumInfo: TextView
    private lateinit var layoutRankingSection: View

    private var heatmapAdapter: RecentAchieveHeatmapAdapter? = null
    private var tooltipDismissRunnable: Runnable? = null

    private var currentRankingScope: String = "WEEKLY"
    private var isRankingExpanded: Boolean = false
    private var rankingAllItems: List<GroupRankingItem> = emptyList()
    private var rankingMyItem: GroupRankingItem? = null
    private var isRankingLoading: Boolean = false
    private var isPremiumLocked: Boolean = false

    private var isMember: Boolean = false
    private var ownerLeaveBlocked: Boolean = false
    private var memberCount: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backClick = View.OnClickListener {
            if (!findNavController().popBackStack()) {
                findNavController().navigateUp()
            }
        }

        val backRoot = view.findViewById<View>(R.id.btn_back)
        backRoot?.setOnClickListener(backClick)

        if (backRoot is ViewGroup) {
            for (i in 0 until backRoot.childCount) {
                backRoot.getChildAt(i).setOnClickListener(backClick)
            }
        }

        tvGroupName = view.findViewById(R.id.tv_group_name)
        tvGroupMemberCount = view.findViewById(R.id.tv_group_member_count)
        btnJoin = view.findViewById(R.id.btn_join)
        btnLeave = view.findViewById(R.id.btn_leave_group)
        btnChat = view.findViewById(R.id.btn_chat)
        rvRecentAchieve = view.findViewById(R.id.rv_recent_achieve)
        layoutGoalChartContainer = view.findViewById(R.id.layout_goal_chart_container)

        layoutRecentAchieveRoot = view.findViewById(R.id.layout_recent_achieve_root)
        layoutHeatmapTooltip = view.findViewById(R.id.layout_heatmap_tooltip)
        tvTooltipDay = view.findViewById(R.id.tv_tooltip_day)
        tvTooltipCount = view.findViewById(R.id.tv_tooltip_count)
        viewTooltipDot = view.findViewById(R.id.view_tooltip_dot)

        btnToggleWeek = view.findViewById(R.id.btn_toggle_week)
        btnToggleAll = view.findViewById(R.id.btn_toggle_all)
        viewToggleSelected = view.findViewById(R.id.view_toggle_selected)

        layoutRankingList = view.findViewById(R.id.layout_ranking_list)
        layoutMore = view.findViewById(R.id.layout_more)
        btnMore = view.findViewById(R.id.btn_more)
        ivMoreArrow = view.findViewById(R.id.iv_more_arrow)

        tvRecentAchieveTitle = view.findViewById(R.id.tv_recent_achieve_title)
        layoutRankingHeader = view.findViewById(R.id.layout_ranking_header)
        layoutRankingSection = view.findViewById(R.id.layout_ranking_section)

        layoutPremiumLocked = view.findViewById(R.id.layout_premium_locked)
        btnPremiumInfo = view.findViewById(R.id.btn_premium_info)

        btnPremiumInfo.setOnClickListener {
            startActivity(Intent(requireContext(), PremiumSubscribeActivity::class.java))
        }

        rvRecentAchieve.layoutManager = GridLayoutManager(requireContext(), 10)
        rvRecentAchieve.setHasFixedSize(false)
        rvRecentAchieve.isNestedScrollingEnabled = false

        tvGroupName.text = args.groupName ?: "Group"
        memberCount = args.memberCount
        tvGroupMemberCount.text = if (memberCount > 0) "${memberCount}명 참여 중" else ""

        btnLeave.visibility = View.GONE
        applyMembershipUi()
        updateRankingTabUi(immediate = true)

        btnJoin.setOnClickListener {
            if (isPremiumLocked) {
                showPremiumLockedUi()
                return@setOnClickListener
            }

            if (isMember) {
                Toast.makeText(requireContext(), "이미 가입된 그룹이에요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showJoinDialog()
        }

        btnLeave.setOnClickListener {
            if (!isMember) {
                Toast.makeText(requireContext(), "아직 가입되지 않은 그룹이에요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showLeaveConfirmDialog()
        }

        btnChat.setOnClickListener {
            if (isPremiumLocked) {
                showPremiumLockedUi()
                return@setOnClickListener
            }

            if (!isMember) {
                Toast.makeText(requireContext(), "그룹에 가입한 후 채팅할 수 있어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val action = GroupStatsFragmentDirections
                .actionGroupStatsFragmentToGroupChattingFragment(
                    groupId = args.groupId,
                    groupName = args.groupName,
                    memberCount = memberCount
                )
            findNavController().navigate(action)
        }

        btnToggleWeek.setOnClickListener {
            if (isPremiumLocked) return@setOnClickListener
            if (currentRankingScope == "WEEKLY" || isRankingLoading) return@setOnClickListener
            currentRankingScope = "WEEKLY"
            isRankingExpanded = false
            updateRankingTabUi()
            loadRanking()
        }

        btnToggleAll.setOnClickListener {
            if (isPremiumLocked) return@setOnClickListener
            if (currentRankingScope == "TOTAL" || isRankingLoading) return@setOnClickListener
            currentRankingScope = "TOTAL"
            isRankingExpanded = false
            updateRankingTabUi()
            loadRanking()
        }

        layoutMore.setOnClickListener {
            if (isPremiumLocked) return@setOnClickListener
            isRankingExpanded = !isRankingExpanded
            bindRankingList()
        }

        layoutRecentAchieveRoot.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val touchedView = rvRecentAchieve.findChildViewUnder(event.x, event.y)
                if (touchedView == null) hideHeatmapTooltip()
            }
            false
        }

        refreshMembershipStateAndLoadStats()
    }

    private fun applyMembershipUi() {
        if (isPremiumLocked) {
            btnJoin.visibility = View.GONE
            btnLeave.visibility = View.GONE
            return
        }

        btnJoin.visibility = View.VISIBLE

        if (isMember) {
            btnJoin.text = "가입완료"
            btnJoin.isEnabled = false
            btnJoin.alpha = 0.6f

            btnLeave.visibility =
                if (ownerLeaveBlocked) View.GONE else View.VISIBLE

        } else {
            btnJoin.text = getString(R.string.group_join)
            btnJoin.isEnabled = true
            btnJoin.alpha = 1f

            btnLeave.visibility = View.GONE
        }
    }

    private fun refreshMembershipStateAndLoadStats() {
        val token = TokenProvider.getBearerToken(requireContext())
        if (token.isNullOrBlank()) {
            isMember = false
            applyMembershipUi()
            rankingAllItems = emptyList()
            rankingMyItem = null
            bindRankingList()
            return
        }

        val groupId = args.groupId

        lifecycleScope.launch {
            val myGroupsResult = withContext(Dispatchers.IO) {
                runCatching { networkService.getMyGroups(token) }
            }

            val myGroupsError = myGroupsResult.exceptionOrNull()

            if (myGroupsError is retrofit2.HttpException && myGroupsError.code() == 403) {
                showPremiumLockedUi()
                return@launch
            }

            val myGroups = myGroupsResult.getOrElse { emptyList() }

            val allGroups = withContext(Dispatchers.IO) {
                runCatching { networkService.getAllGroups(token) }.getOrElse { emptyList() }
            }

            isMember = myGroups.any { it.id == groupId }
            ownerLeaveBlocked = false
            applyMembershipUi()

            val currentGroup = myGroups.find { it.id == groupId }
                ?: allGroups.find { it.id == groupId }

            memberCount = currentGroup?.memberCount ?: memberCount
            tvGroupMemberCount.text = if (memberCount > 0) "${memberCount}명 참여 중" else ""

            loadStats(token, groupId)

            if (isMember) {
                loadRanking()
            } else {
                rankingAllItems = emptyList()
                rankingMyItem = null
                bindRankingList()
            }
        }
    }

    private fun updateRankingTabUi(immediate: Boolean = false) {
        val isWeekly = currentRankingScope == "WEEKLY"

        btnToggleWeek.setTextColor(
            if (isWeekly) Color.WHITE else ContextCompat.getColor(requireContext(), R.color.black_30)
        )

        btnToggleAll.setTextColor(
            if (isWeekly) ContextCompat.getColor(requireContext(), R.color.black_30) else Color.WHITE
        )

        viewToggleSelected.post {
            val toggleRoot = view?.findViewById<View>(R.id.toggle_root) ?: return@post
            val halfWidth = toggleRoot.width / 2f
            val targetX = if (isWeekly) 0f else halfWidth

            if (immediate) {
                viewToggleSelected.translationX = targetX
            } else {
                viewToggleSelected.animate()
                    .translationX(targetX)
                    .setDuration(180L)
                    .start()
            }
        }
    }

    private fun loadRanking() {
        if (isPremiumLocked) {
            rankingAllItems = emptyList()
            rankingMyItem = null
            bindRankingList()
            return
        }

        if (!isMember) {
            rankingAllItems = emptyList()
            rankingMyItem = null
            bindRankingList()

            Toast.makeText(
                requireContext(),
                "그룹 가입 후 랭킹을 확인할 수 있어요.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val token = TokenProvider.getBearerToken(requireContext())
        if (token.isNullOrBlank()) {
            rankingAllItems = emptyList()
            rankingMyItem = null
            bindRankingList()
            return
        }

        if (isRankingLoading) return
        isRankingLoading = true

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    networkService.getGroupRanking(
                        token = token,
                        groupId = args.groupId,
                        scope = currentRankingScope
                    )
                }

                if (resp.isSuccessful) {
                    if (!isPremiumLocked) {
                        hidePremiumLockedUi()
                    }

                    val body = resp.body()
                    rankingAllItems = body?.items.orEmpty()

                    rankingMyItem = body?.let {
                        GroupRankingItem(
                            rank = it.myRank ?: 0,
                            memberId = it.memberId,
                            nickname = it.myName,
                            profileImage = it.profileImage,
                            achieveCount = it.achieveCount ?: 0
                        )
                    }
                } else {
                    rankingAllItems = emptyList()
                    rankingMyItem = null

                    if (resp.code() == 403) {
                        showPremiumLockedUi()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "랭킹 조회 실패: ${resp.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                rankingAllItems = emptyList()
                rankingMyItem = null
                Toast.makeText(
                    requireContext(),
                    "랭킹 표시 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isRankingLoading = false
                bindRankingList()
            }
        }
    }

    private fun bindRankingList() {
        if (isPremiumLocked) {
            layoutRankingList.removeAllViews()
            layoutRankingList.visibility = View.GONE
            layoutMore.visibility = View.GONE
            return
        }

        layoutRankingList.removeAllViews()

        val myItem = rankingMyItem
        val otherItems = rankingAllItems.filterNot { item ->
            myItem?.memberId != null && item.memberId == myItem.memberId
        }

        val visibleOtherItems = if (isRankingExpanded) otherItems else otherItems.take(5)

        myItem?.let {
            addRankingRow(item = it, isMine = true)
        }

        visibleOtherItems.forEach {
            addRankingRow(item = it, isMine = false)
        }

        val hasAnyRanking = myItem != null || visibleOtherItems.isNotEmpty()
        layoutRankingList.visibility = if (hasAnyRanking) View.VISIBLE else View.GONE

        layoutMore.visibility = if (otherItems.size > 5) View.VISIBLE else View.GONE
        btnMore.text = if (isRankingExpanded) "접기" else getString(R.string.more)
        ivMoreArrow.rotation = if (isRankingExpanded) 180f else 0f
    }

    private fun addRankingRow(item: GroupRankingItem, isMine: Boolean) {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_group_ranking_row, layoutRankingList, false)

        val highlightBg = row.findViewById<View>(R.id.view_highlight_bg)
        val tvRank = row.findViewById<TextView>(R.id.tv_rank)
        val ivProfile = row.findViewById<ImageView>(R.id.iv_profile)
        val tvName = row.findViewById<TextView>(R.id.tv_name)
        val tvCount = row.findViewById<TextView>(R.id.tv_count)

        highlightBg.visibility = if (isMine) View.VISIBLE else View.INVISIBLE
        tvRank.text = if (item.rank > 0) item.rank.toString() else "-"
        tvName.text = item.nickname?.takeIf { it.isNotBlank() } ?: "이름 없음"
        tvCount.text = item.achieveCount.toString()

        if (!item.profileImage.isNullOrBlank()) {
            Glide.with(this)
                .load(item.profileImage)
                .placeholder(R.drawable.bg_circle)
                .error(R.drawable.bg_circle)
                .circleCrop()
                .into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.bg_circle)
        }

        layoutRankingList.addView(row)
    }

    private fun showJoinDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_group_join, null, false)

        val etPassword: EditText = dialogView.findViewById(R.id.etPassword)
        val tvWrong: TextView = dialogView.findViewById(R.id.tvPwLabel_wrong)
        val btnCancel: TextView = dialogView.findViewById(R.id.btnCancel)
        val btnJoinInDialog: TextView = dialogView.findViewById(R.id.btn_join)

        tvWrong.visibility = View.INVISIBLE

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            setCancelable(true)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnJoinInDialog.setOnClickListener {
            tvWrong.visibility = View.INVISIBLE
            val pw = etPassword.text?.toString()?.trim().orEmpty()
            dialog.dismiss()
            requestJoinGroup(passwordOrBlank = pw)
        }

        dialog.show()
    }

    private fun requestJoinGroup(passwordOrBlank: String) {
        val token = TokenProvider.getBearerToken(requireContext())
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val password = passwordOrBlank.ifBlank { null }

        lifecycleScope.launch {
            val resp: Response<ResponseBody> = withContext(Dispatchers.IO) {
                networkService.joinGroup(
                    token = token,
                    groupId = args.groupId,
                    password = password
                )
            }

            if (!resp.isSuccessful) {
                val err = safeBodyString(resp.errorBody())

                if (resp.code() == 403) {
                    showPremiumLockedUi()
                    return@launch
                }

                if (resp.code() == 400 && err.contains("password", ignoreCase = true)) {
                    Toast.makeText(requireContext(), "비밀번호가 필요하거나 올바르지 않습니다.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                if (resp.code() == 409 || err.contains("already", ignoreCase = true)) {
                    isMember = true
                    applyMembershipUi()
                    Toast.makeText(requireContext(), "이미 가입된 그룹이에요.", Toast.LENGTH_SHORT).show()
                    refreshMembershipStateAndLoadStats()
                    return@launch
                }

                Toast.makeText(requireContext(), "가입 실패: ${resp.code()}\n$err", Toast.LENGTH_LONG).show()
                return@launch
            }

            Toast.makeText(requireContext(), "그룹 가입 완료!", Toast.LENGTH_SHORT).show()
            isMember = true
            applyMembershipUi()
            refreshMembershipStateAndLoadStats()
        }
    }

    private fun showLeaveConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("그룹 탈퇴")
            .setMessage("정말 이 그룹에서 탈퇴할까요?")
            .setNegativeButton("취소", null)
            .setPositiveButton("탈퇴") { _, _ -> requestLeaveGroup() }
            .show()
    }

    private fun requestLeaveGroup() {
        val token = TokenProvider.getBearerToken(requireContext())
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val resp: Response<ResponseBody> = withContext(Dispatchers.IO) {
                networkService.leaveGroup(token = token, groupId = args.groupId)
            }

            if (!resp.isSuccessful) {
                val err = safeBodyString(resp.errorBody())

                if (resp.code() == 500 && err.contains("OWNER", ignoreCase = true)) {
                    ownerLeaveBlocked = true
                    applyMembershipUi()
                    Toast.makeText(
                        requireContext(),
                        "그룹장은 탈퇴할 수 없어요. (삭제/권한위임 필요)",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                Toast.makeText(requireContext(), "탈퇴 실패: ${resp.code()}\n$err", Toast.LENGTH_LONG).show()
                return@launch
            }

            Toast.makeText(requireContext(), "그룹에서 탈퇴했습니다.", Toast.LENGTH_SHORT).show()
            isMember = false
            ownerLeaveBlocked = false
            applyMembershipUi()
            findNavController().popBackStack()
        }
    }

    private fun loadStats(token: String, groupId: Long) {
        lifecycleScope.launch {
            val meDeferred = async(Dispatchers.IO) {
                runCatching { networkService.getMe(token) }.getOrNull()
            }

            val sleepDeferred = async(Dispatchers.IO) {
                runCatching { networkService.getGroupSleepStats(token, groupId) }
                    .getOrNull()
            }

            val goalsDeferred = async(Dispatchers.IO) {
                runCatching { networkService.getGroupGoalsProgress(token, groupId) }
                    .getOrNull()
            }

            val heatmapDeferred = async(Dispatchers.IO) {
                runCatching { networkService.getGoalHeatmap(token, groupId) }
                    .getOrNull()
            }

            val myEmail = meDeferred.await()?.body()?.email
            val sleepResp = sleepDeferred.await()
            val goalsResp = goalsDeferred.await()
            val heatmapResp = heatmapDeferred.await()

            if (sleepResp?.code() == 403 || goalsResp?.code() == 403 || heatmapResp?.code() == 403) {
                showPremiumLockedUi()
                return@launch
            }

            if (!isPremiumLocked) {
                hidePremiumLockedUi()
            }

            val sleepBody = sleepResp?.body()
            val userSleepMinutes = sleepBody?.userSleepDurations.orEmpty()
            val groupSleepMinutes = sleepBody?.groupAverageSleepDurations.orEmpty()

            bindDynamicGoalCharts(
                goals = goalsResp?.body().orEmpty(),
                userSleepMinutes = userSleepMinutes,
                groupSleepMinutes = groupSleepMinutes,
                myEmail = myEmail
            )

            bindHeatmap(heatmapResp?.body().orEmpty())
        }
    }

    private fun showPremiumLockedUi() {
        isPremiumLocked = true

        layoutPremiumLocked.visibility = View.VISIBLE

        layoutGoalChartContainer.removeAllViews()
        layoutGoalChartContainer.visibility = View.GONE

        tvRecentAchieveTitle.visibility = View.GONE
        layoutRecentAchieveRoot.visibility = View.GONE

        layoutRankingHeader.visibility = View.GONE
        layoutRankingSection.visibility = View.GONE

        layoutRankingList.removeAllViews()
        layoutRankingList.visibility = View.GONE

        layoutMore.visibility = View.GONE

        btnChat.visibility = View.GONE

        rankingAllItems = emptyList()
        rankingMyItem = null

        applyMembershipUi()
    }


    private fun hidePremiumLockedUi() {
        if (isPremiumLocked) return

        layoutPremiumLocked.visibility = View.GONE

        layoutGoalChartContainer.visibility = View.VISIBLE

        tvRecentAchieveTitle.visibility = View.VISIBLE
        layoutRecentAchieveRoot.visibility = View.VISIBLE

        layoutRankingHeader.visibility = View.VISIBLE
        layoutRankingSection.visibility = View.VISIBLE

        layoutRankingList.visibility = View.VISIBLE

        btnChat.visibility = View.VISIBLE
    }

    private fun bindDynamicGoalCharts(
        goals: List<GroupGoalProgressResponseItem>,
        userSleepMinutes: List<Int>,
        groupSleepMinutes: List<Int>,
        myEmail: String?
    ) {
        if (isPremiumLocked) return

        layoutGoalChartContainer.removeAllViews()

        goals.firstOrNull { it.goalName.contains("수면") }?.let {
            addSleepItem(it, userSleepMinutes, groupSleepMinutes)
        }

        goals.firstOrNull { it.goalName.contains("뽀모도로") }?.let {
            addPomodoroItem(it, myEmail)
        }

        layoutGoalChartContainer.visibility =
            if (layoutGoalChartContainer.childCount > 0) View.VISIBLE else View.GONE
    }

    private fun bindExternalXAxisLabels(container: View, labels: List<String>, isBarChart: Boolean, chart: View) {
        val labelContainerId = if (isBarChart) R.id.layout_bar_x_labels else R.id.layout_line_x_labels

        val visibleIds = if (isBarChart) {
            listOf(R.id.tv_bar_x_1, R.id.tv_bar_x_2, R.id.tv_bar_x_3, R.id.tv_bar_x_4, R.id.tv_bar_x_5, R.id.tv_bar_x_6)
        } else {
            listOf(R.id.tv_line_x_1, R.id.tv_line_x_2, R.id.tv_line_x_3, R.id.tv_line_x_4, R.id.tv_line_x_5, R.id.tv_line_x_6)
        }

        val labelContainer = container.findViewById<LinearLayout>(labelContainerId)

        visibleIds.forEachIndexed { index, id ->
            container.findViewById<TextView>(id)?.apply {
                visibility = View.VISIBLE
                text = labels.getOrNull(index).orEmpty()
            }
        }

        chart.post {
            val contentRect = when (chart) {
                is LineChart -> chart.viewPortHandler.contentRect
                is CombinedChart -> chart.viewPortHandler.contentRect
                else -> null
            } ?: return@post

            labelContainer.setPadding(
                contentRect.left.toInt(),
                0,
                (chart.width - contentRect.right).toInt(),
                0
            )
        }
    }

    private fun addSleepItem(
        goal: GroupGoalProgressResponseItem,
        userSleepMinutes: List<Int>,
        groupSleepMinutes: List<Int>
    ) {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_group_goal_chart, layoutGoalChartContainer, false)

        val tvMinGoalValue = itemView.findViewById<TextView>(R.id.tv_min_goal_value)
        val tvGoalTitle = itemView.findViewById<TextView>(R.id.tv_goal_stat_title)
        val tvGoalDesc = itemView.findViewById<TextView>(R.id.tv_goal_stat_desc)
        val legend = itemView.findViewById<View>(R.id.layout_goal_legend)
        val lineCard = itemView.findViewById<View>(R.id.card_line_chart)
        val barCard = itemView.findViewById<View>(R.id.card_bar_chart)
        val lineChart = itemView.findViewById<LineChart>(R.id.chart_line)

        legend.visibility = View.VISIBLE
        lineCard.visibility = View.VISIBLE
        barCard.visibility = View.GONE

        tvMinGoalValue.text = "수면 ${goal.goalValue}시간 이상"
        tvGoalTitle.text = "수면시간 통계"

        if (userSleepMinutes.isEmpty() && groupSleepMinutes.isEmpty()) {
            tvGoalDesc.text = "수면 데이터가 없습니다"
            lineChart.clear()
            layoutGoalChartContainer.addView(itemView)
            return
        }

        val xLabels = buildLast6DayLabels()
        val userSleepHours = normalizeSleepMinutesToSixHours(userSleepMinutes)
        val groupSleepHours = normalizeSleepMinutesToSixHours(groupSleepMinutes)

        val userAvgMinutes = userSleepMinutes.takeLast(6)
            .filter { it > 0 }
            .average()
            .takeIf { !it.isNaN() }
            ?: 0.0

        tvGoalDesc.text = if (userAvgMinutes > 0.0) {
            "최근 평균 수면: ${"%.1f".format(userAvgMinutes / 60.0)}시간"
        } else {
            "최근 평균 수면 데이터가 없습니다"
        }

        renderSleepChart(
            chart = lineChart,
            userValues = userSleepHours,
            groupValues = groupSleepHours,
            xLabels = xLabels,
            goalY = goal.goalValue.toFloat(),
            avgHour = (userAvgMinutes / 60.0).toFloat(),
            participantCount = memberCount
        )

        bindExternalXAxisLabels(itemView, xLabels, false, lineChart)
        layoutGoalChartContainer.addView(itemView)
    }

    private fun addPomodoroItem(goal: GroupGoalProgressResponseItem, myEmail: String?) {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_group_goal_chart, layoutGoalChartContainer, false)

        val tvMinGoalValue = itemView.findViewById<TextView>(R.id.tv_min_goal_value)
        val tvGoalTitle = itemView.findViewById<TextView>(R.id.tv_goal_stat_title)
        val tvGoalDesc = itemView.findViewById<TextView>(R.id.tv_goal_stat_desc)
        val legend = itemView.findViewById<View>(R.id.layout_goal_legend)
        val lineCard = itemView.findViewById<View>(R.id.card_line_chart)
        val barCard = itemView.findViewById<View>(R.id.card_bar_chart)
        val barChart = itemView.findViewById<CombinedChart>(R.id.chart_bar)

        legend.visibility = View.VISIBLE
        lineCard.visibility = View.GONE
        barCard.visibility = View.VISIBLE

        val minGoalValue = goal.goalValue.toFloat()

        tvMinGoalValue.text = "뽀모도로 ${minGoalValue.toInt()}회 이상"
        tvGoalTitle.text = "뽀모도로 통계"
        tvGoalDesc.text = "달성 횟수 통계"

        val rawValues = goal.userProgress.map { (it.progress ?: 0).toFloat() }

        if (rawValues.isEmpty()) {
            barChart.clear()
            layoutGoalChartContainer.addView(itemView)
            return
        }

        val myProgress = goal.userProgress
            .firstOrNull { it.userEmail == myEmail }
            ?.progress
            ?.toFloat()
            ?: rawValues.average().toFloat()

        val xLabels = buildLast6DayLabels()
        val groupValues = normalizePomodoroToSixBars(rawValues, minGoalValue)
        val myValues = normalizePomodoroLineToSix(groupValues, myProgress)

        renderPomodoroChart(barChart, groupValues, myValues, xLabels, minGoalValue)
        bindExternalXAxisLabels(itemView, xLabels, true, barChart)

        layoutGoalChartContainer.addView(itemView)
    }

    private fun buildLast6DayLabels(): List<String> {
        val formatter = DateTimeFormatter.ofPattern("d")
        val today = LocalDate.now()

        return (5 downTo 0).map { offset ->
            today.minusDays(offset.toLong()).format(formatter)
        }
    }

    private fun normalizeSleepMinutesToSixHours(values: List<Int>): List<Float> {
        val lastSix = values.takeLast(6).map { it / 60f }
        return if (lastSix.size >= 6) lastSix else List(6 - lastSix.size) { 0f } + lastSix
    }

    private fun normalizePomodoroToSixBars(values: List<Float>, goal: Float): List<Float> {
        if (values.isEmpty()) return List(6) { 0f }

        val actual = values.takeLast(6)
        return if (actual.size >= 6) actual else List(6 - actual.size) { 0f } + actual
    }

    private fun normalizePomodoroLineToSix(groupValues: List<Float>, myProgress: Float): List<Float> {
        return List(6) { myProgress }
    }

    private fun renderSleepChart(
        chart: LineChart,
        userValues: List<Float>,
        groupValues: List<Float>,
        xLabels: List<String>,
        goalY: Float,
        avgHour: Float,
        participantCount: Int
    ) {
        val userEntries = userValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val groupEntries = groupValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val maxValue = max(max(userValues.maxOrNull() ?: 0f, groupValues.maxOrNull() ?: 0f), goalY)
        val yMax = max(12f, ceil(maxValue + 1f))

        ChartStyle.applySleep(chart, xLabels, goalY, 0f, yMax)

        val groupSet = LineDataSet(groupEntries, "").apply {
            color = ChartStyle.lineColor()
            lineWidth = 2.2f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(false)
            mode = LineDataSet.Mode.LINEAR
            setDrawHighlightIndicators(false)
        }

        val userSet = LineDataSet(userEntries, "").apply {
            color = ChartStyle.goalColor()
            lineWidth = 2.6f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(false)
            mode = LineDataSet.Mode.LINEAR
            setDrawHighlightIndicators(false)
        }

        val lastUserEntry = userEntries.lastOrNull()
        val pointSet = lastUserEntry?.let {
            LineDataSet(listOf(it), "").apply {
                color = ChartStyle.goalColor()
                lineWidth = 0f
                setDrawValues(false)
                setDrawFilled(false)
                setDrawCircles(true)
                circleRadius = 7f
                circleHoleRadius = 3.6f
                setCircleColor(ChartStyle.goalColor())
                circleHoleColor = ChartStyle.circleHoleColor()
                setDrawHighlightIndicators(false)
            }
        }

        chart.data = if (pointSet != null) LineData(groupSet, userSet, pointSet) else LineData(groupSet, userSet)
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = 5.5f
        chart.marker = SleepMarkerView(requireContext(), participantCount, avgHour)

        lastUserEntry?.let {
            chart.highlightValue(it.x, it.y, 2)
        }

        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun renderPomodoroChart(
        chart: CombinedChart,
        groupValues: List<Float>,
        myValues: List<Float>,
        xLabels: List<String>,
        goalY: Float
    ) {
        val barEntries = groupValues.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        val lineEntries = myValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val maxValue = max(max(groupValues.maxOrNull() ?: 0f, myValues.maxOrNull() ?: 0f), goalY)
        val yMax = ceil(maxValue + 1f).coerceAtLeast(goalY + 1f)

        ChartStyle.applyPomodoro(chart, xLabels, goalY, 0f, yMax)

        val barDataSet = BarDataSet(barEntries, "").apply {
            color = ChartStyle.barColor()
            setDrawValues(false)
            highLightAlpha = 0
        }

        val lineDataSet = LineDataSet(lineEntries, "").apply {
            color = ChartStyle.lineColor()
            lineWidth = 2.4f
            setDrawValues(false)
            setDrawCircles(false)
            setDrawFilled(true)
            fillDrawable = ChartStyle.makePomodoroFillDrawable(requireContext())
            mode = LineDataSet.Mode.LINEAR
            setDrawHighlightIndicators(false)
        }

        val pointSet = LineDataSet(listOf(lineEntries.last()), "").apply {
            color = ChartStyle.lineColor()
            lineWidth = 0f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(true)
            circleRadius = 5.5f
            circleHoleRadius = 3f
            setCircleColor(ChartStyle.lineColor())
            circleHoleColor = ChartStyle.circleHoleColor()
            setDrawHighlightIndicators(false)
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.18f
        }

        chart.data = CombinedData().apply {
            setData(barData)
            setData(LineData(lineDataSet, pointSet))
        }

        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = 5.5f
        chart.fitScreen()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun bindHeatmap(items: List<GroupAchievementHeatmapItem>) {
        if (isPremiumLocked) return

        val safeItems = if (items.isNotEmpty()) {
            items.takeLast(30)
        } else {
            List(30) { index ->
                GroupAchievementHeatmapItem(
                    date = "2026-02-${(index + 1).toString().padStart(2, '0')}",
                    achievedUserCount = 0
                )
            }
        }

        heatmapAdapter = RecentAchieveHeatmapAdapter(items = safeItems) { item, anchorView, isNowSelected, _ ->
            if (isNowSelected) showHeatmapTooltip(item, anchorView)
            else hideHeatmapTooltip(clearSelection = false)
        }

        rvRecentAchieve.adapter = heatmapAdapter
        hideHeatmapTooltip(clearSelection = false)
    }

    private fun showHeatmapTooltip(item: GroupAchievementHeatmapItem, anchorView: View) {
        val dayText = item.date.substringAfterLast("-").toIntOrNull()?.toString() ?: item.date
        tvTooltipDay.text = dayText
        tvTooltipCount.text = "${item.achievedUserCount}명"
        viewTooltipDot.visibility = View.VISIBLE
        layoutHeatmapTooltip.visibility = View.VISIBLE

        layoutRecentAchieveRoot.post {
            val rootLocation = IntArray(2)
            val anchorLocation = IntArray(2)

            layoutRecentAchieveRoot.getLocationOnScreen(rootLocation)
            anchorView.getLocationOnScreen(anchorLocation)

            val anchorXInRoot = anchorLocation[0] - rootLocation[0]
            val anchorYInRoot = anchorLocation[1] - rootLocation[1]

            val tooltipWidth = layoutHeatmapTooltip.width.takeIf { it > 0 } ?: dpToPx(48)
            val tooltipHeight = layoutHeatmapTooltip.height.takeIf { it > 0 } ?: dpToPx(48)

            val anchorCenterX = anchorXInRoot + anchorView.width / 2

            var targetX = anchorCenterX - tooltipWidth / 2
            var targetY = anchorYInRoot - tooltipHeight - dpToPx(8)

            targetX = targetX.coerceIn(0, layoutRecentAchieveRoot.width - tooltipWidth)

            if (targetY < 0) {
                targetY = anchorYInRoot + anchorView.height + dpToPx(8)
            }

            layoutHeatmapTooltip.x = targetX.toFloat()
            layoutHeatmapTooltip.y = targetY.toFloat()
        }

        tooltipDismissRunnable?.let { layoutHeatmapTooltip.removeCallbacks(it) }
    }

    private fun hideHeatmapTooltip(clearSelection: Boolean = true) {
        layoutHeatmapTooltip.visibility = View.GONE
        if (clearSelection) heatmapAdapter?.clearSelection()
        tooltipDismissRunnable?.let { layoutHeatmapTooltip.removeCallbacks(it) }
        tooltipDismissRunnable = null
    }

    private fun safeBodyString(body: ResponseBody?): String {
        return runCatching { body?.string().orEmpty() }.getOrDefault("")
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private inner class SleepMarkerView(
        context: Context,
        private val participantCount: Int,
        private val avgHour: Float
    ) : MarkerView(context, android.R.layout.simple_list_item_1) {

        private val root: LinearLayout
        private val title: TextView
        private val sub: TextView

        init {
            removeAllViews()

            root = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                background = ChartStyle.makeMarkerBackground()
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }

            title = TextView(context).apply {
                text = "평균 수면시간"
                setTextColor(Color.BLACK)
                textSize = 13f
                typeface = Typeface.DEFAULT
            }

            sub = TextView(context).apply {
                text = "${participantCount}명 참여   ${"%.1f".format(avgHour)}시간"
                setTextColor(ChartStyle.goalColor())
                textSize = 11f
                setPadding(0, dp(8), 0, 0)
            }

            root.addView(title)
            root.addView(sub)
            addView(root)
        }

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF((-width - dp(12)).toFloat(), (-height - dp(12)).toFloat())
        }

        override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
            val offsetY = -height - dp(12).toFloat()
            var offsetX = -width - dp(12).toFloat()

            if (posX + offsetX < dp(4)) {
                offsetX = dp(12).toFloat()
            }

            return MPPointF(offsetX, offsetY)
        }

        private fun dp(value: Int): Int {
            return (value * resources.displayMetrics.density).toInt()
        }
    }
}