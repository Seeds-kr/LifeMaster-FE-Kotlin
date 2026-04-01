package com.example.lifemaster.presentation.group.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.example.lifemaster.presentation.group.model.UserProgressItem
import com.example.lifemaster.presentation.group.util.ChartStyle
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
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

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

    private var heatmapAdapter: RecentAchieveHeatmapAdapter? = null
    private var tooltipDismissRunnable: Runnable? = null

    private var currentRankingScope: String = "WEEKLY"
    private var isRankingExpanded: Boolean = false
    private var rankingAllItems: List<GroupRankingItem> = emptyList()
    private var rankingMyRank: Int? = null
    private var isRankingLoading: Boolean = false

    private var isMember: Boolean = false
    private var ownerLeaveBlocked: Boolean = false
    private var memberCount: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        rvRecentAchieve.layoutManager = GridLayoutManager(requireContext(), 10)
        rvRecentAchieve.setHasFixedSize(true)
        rvRecentAchieve.isNestedScrollingEnabled = false

        tvGroupName.text = args.groupName ?: "Group"
        memberCount = args.memberCount
        tvGroupMemberCount.text = if (memberCount > 0) "${memberCount}명 참여 중" else ""

        btnLeave.visibility = View.GONE
        applyMembershipUi()
        updateRankingTabUi(immediate = true)

        btnJoin.setOnClickListener {
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
            if (currentRankingScope == "WEEKLY" || isRankingLoading) return@setOnClickListener
            currentRankingScope = "WEEKLY"
            isRankingExpanded = false
            updateRankingTabUi()
            loadRanking()
        }

        btnToggleAll.setOnClickListener {
            if (currentRankingScope == "TOTAL" || isRankingLoading) return@setOnClickListener
            currentRankingScope = "TOTAL"
            isRankingExpanded = false
            updateRankingTabUi()
            loadRanking()
        }

        layoutMore.setOnClickListener {
            isRankingExpanded = !isRankingExpanded
            bindRankingList()
        }

        layoutRecentAchieveRoot.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val touchedView = rvRecentAchieve.findChildViewUnder(event.x, event.y)
                if (touchedView == null) {
                    hideHeatmapTooltip()
                }
            }
            false
        }

        refreshMembershipStateAndLoadStats()
    }

    private fun applyMembershipUi() {
        if (isMember) {
            btnJoin.text = "가입완료"
            btnJoin.isEnabled = false
            btnJoin.alpha = 0.6f
            btnLeave.visibility = if (ownerLeaveBlocked) View.GONE else View.VISIBLE
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
            rankingMyRank = null
            bindRankingList()
            return
        }

        val groupId = args.groupId

        lifecycleScope.launch {
            val myGroups = withContext(Dispatchers.IO) {
                runCatching { networkService.getMyGroups(token) }.getOrElse { emptyList() }
            }

            val allGroups = withContext(Dispatchers.IO) {
                runCatching { networkService.getAllGroups(token) }.getOrElse { emptyList() }
            }

            isMember = myGroups.any { it.id == groupId }
            ownerLeaveBlocked = false
            applyMembershipUi()

            val currentGroup = myGroups.find { it.id == groupId }
                ?: allGroups.find { it.id == groupId }

            val latestCount = currentGroup?.memberCount ?: memberCount
            memberCount = latestCount
            tvGroupMemberCount.text = if (memberCount > 0) "${memberCount}명 참여 중" else ""

            loadStats(token, groupId)
            loadRanking()
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
        val token = TokenProvider.getBearerToken(requireContext())
        if (token.isNullOrBlank()) {
            rankingAllItems = emptyList()
            rankingMyRank = null
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

                if (!isAdded) return@launch

                if (resp.isSuccessful) {
                    val body = resp.body()
                    rankingAllItems = body?.items.orEmpty()
                    rankingMyRank = body?.myRank
                } else {
                    rankingAllItems = emptyList()
                    rankingMyRank = null
                }

                bindRankingList()
            } catch (e: Exception) {
                if (!isAdded) return@launch
                rankingAllItems = emptyList()
                rankingMyRank = null
                bindRankingList()
            } finally {
                isRankingLoading = false
            }
        }
    }

    private fun bindRankingList() {
        val reorderedItems = reorderRankingItems(rankingAllItems, rankingMyRank)
        val visibleItems = if (isRankingExpanded) reorderedItems else reorderedItems.take(5)

        layoutRankingList.removeAllViews()

        visibleItems.forEach { item ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_group_ranking_row, layoutRankingList, false)

            val highlightBg = row.findViewById<View>(R.id.view_highlight_bg)
            val tvRank = row.findViewById<TextView>(R.id.tv_rank)
            val ivProfile = row.findViewById<ImageView>(R.id.iv_profile)
            val tvName = row.findViewById<TextView>(R.id.tv_name)
            val tvCount = row.findViewById<TextView>(R.id.tv_count)

            val isMine = rankingMyRank != null && item.rank == rankingMyRank

            highlightBg.visibility = if (isMine) View.VISIBLE else View.INVISIBLE
            tvRank.text = item.rank.toString()
            tvName.text = item.nickname.ifBlank { "이름 없음" }
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

        layoutMore.visibility = if (reorderedItems.size > 5) View.VISIBLE else View.GONE
        btnMore.text = if (isRankingExpanded) "접기" else getString(R.string.more)
        ivMoreArrow.rotation = if (isRankingExpanded) 180f else 0f
    }

    private fun reorderRankingItems(
        items: List<GroupRankingItem>,
        myRank: Int?
    ): List<GroupRankingItem> {
        if (items.isEmpty() || myRank == null) return items

        val myItem = items.firstOrNull { it.rank == myRank } ?: return items
        val others = items.filterNot { it.rank == myRank }

        return listOf(myItem) + others
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

        val groupId = args.groupId
        val password = passwordOrBlank.ifBlank { null }

        lifecycleScope.launch {
            val resp: Response<ResponseBody> = withContext(Dispatchers.IO) {
                networkService.joinGroup(
                    token = token,
                    groupId = groupId,
                    password = password
                )
            }

            if (!resp.isSuccessful) {
                val err = safeBodyString(resp.errorBody())

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

        val groupId = args.groupId

        lifecycleScope.launch {
            val resp: Response<ResponseBody> = withContext(Dispatchers.IO) {
                networkService.leaveGroup(token = token, groupId = groupId)
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
                    .getOrElse { Response.success(GroupSleepStatsResponse(emptyList())) }
            }

            val goalsDeferred = async(Dispatchers.IO) {
                runCatching { networkService.getGroupGoalsProgress(token, groupId) }
                    .getOrElse { Response.success(emptyList()) }
            }

            val heatmapDeferred = async(Dispatchers.IO) {
                runCatching { networkService.getGoalHeatmap(token, groupId) }
                    .getOrElse { Response.success(emptyList()) }
            }

            val myEmail = meDeferred.await()?.body()?.email
            val sleepResp = sleepDeferred.await()
            val goalsResp = goalsDeferred.await()
            val heatmapResp = heatmapDeferred.await()

            val sleepMinutes = sleepResp.body()?.userSleepDurations.orEmpty()
            val goals = goalsResp.body().orEmpty()
            val heatmapItems = heatmapResp.body().orEmpty()

            bindDynamicGoalCharts(
                goals = goals,
                sleepMinutes = sleepMinutes,
                myEmail = myEmail
            )

            bindHeatmap(heatmapItems)
        }
    }

    private fun bindDynamicGoalCharts(
        goals: List<GroupGoalProgressResponseItem>,
        sleepMinutes: List<Int>,
        myEmail: String?
    ) {
        layoutGoalChartContainer.removeAllViews()

        goals.forEach { goal ->
            val goalType = resolveGoalType(goal)

            when (goalType.chartType) {
                GoalChartType.LINE -> addLineGoalItem(goal, goalType, sleepMinutes)
                GoalChartType.BAR -> addBarGoalItem(goal, goalType, myEmail)
            }
        }

        layoutGoalChartContainer.visibility =
            if (layoutGoalChartContainer.childCount > 0) View.VISIBLE else View.GONE
    }

    private fun addLineGoalItem(
        goal: GroupGoalProgressResponseItem,
        goalType: GoalTypeUi,
        sleepMinutes: List<Int>
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

        legend.visibility = View.GONE
        lineCard.visibility = View.VISIBLE
        barCard.visibility = View.GONE

        tvMinGoalValue.text = buildGoalValueText(goal, goalType)
        tvGoalTitle.text = "${goalType.title} 통계"

        if (goalType.isSleep) {
            val values = sleepMinutes.ifEmpty { listOf() }
            if (values.isEmpty()) {
                tvGoalDesc.text = "수면 데이터가 없습니다"
                lineChart.clear()
                layoutGoalChartContainer.addView(itemView)
                return
            }

            val xLabels = buildSequentialLabels(values.size)
            val avgHour = values.average() / 60.0
            tvGoalDesc.text = "최근 평균 수면: %.1f시간".format(avgHour)

            renderSleepChart(
                chart = lineChart,
                values = values.map { it / 60f },
                xLabels = xLabels,
                goalY = goal.goalValue.toFloat(),
                markerSubText = "평균 %.1f시간".format(avgHour)
            )
        } else {
            val values = goal.userProgress.map { it.progress ?: 0 }
            if (values.isEmpty()) {
                tvGoalDesc.text = "진행 데이터가 없습니다"
                lineChart.clear()
                layoutGoalChartContainer.addView(itemView)
                return
            }

            val xLabels = buildMemberLabels(goal.userProgress, values.size)
            val avg = values.average()
            tvGoalDesc.text = "최근 평균 진행도: %.1f".format(avg)

            renderGenericLineChart(
                chart = lineChart,
                values = values.map { it.toFloat() },
                xLabels = xLabels,
                goalY = goal.goalValue.toFloat()
            )
        }

        layoutGoalChartContainer.addView(itemView)
    }

    private fun addBarGoalItem(
        goal: GroupGoalProgressResponseItem,
        goalType: GoalTypeUi,
        myEmail: String?
    ) {
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

        tvMinGoalValue.text = buildGoalValueText(goal, goalType)
        tvGoalTitle.text = "${goalType.title} 통계"

        val groupValues = goal.userProgress.map { (it.progress ?: 0).toFloat() }
        if (groupValues.isEmpty()) {
            tvGoalDesc.text = "진행 데이터가 없습니다"
            barChart.clear()
            layoutGoalChartContainer.addView(itemView)
            return
        }

        val myProgress = goal.userProgress
            .firstOrNull { it.userEmail == myEmail }
            ?.progress
            ?.toFloat()
            ?: 0f

        val myLine = List(groupValues.size) { myProgress }
        val xLabels = buildMemberLabels(goal.userProgress, groupValues.size)

        tvGoalDesc.text = "달성 횟수 통계"

        renderPomodoroChart(
            chart = barChart,
            groupValues = groupValues,
            myValues = myLine,
            xLabels = xLabels,
            goalY = goal.goalValue.toFloat()
        )

        layoutGoalChartContainer.addView(itemView)
    }

    private fun resolveGoalType(goal: GroupGoalProgressResponseItem): GoalTypeUi {
        val name = goal.goalName.orEmpty()
        val condition = goal.goalCondition.orEmpty().uppercase()

        return when {
            name.contains("수면") || (condition == "TIME" && name.contains("수면")) ->
                GoalTypeUi("수면시간", true, GoalChartType.LINE)

            condition == "TIME" ->
                GoalTypeUi(extractBaseTitle(name, "시간"), false, GoalChartType.LINE)

            else ->
                GoalTypeUi(extractBaseTitle(name, "회"), false, GoalChartType.BAR)
        }
    }

    private fun buildGoalValueText(goal: GroupGoalProgressResponseItem, type: GoalTypeUi): String {
        return when (type.chartType) {
            GoalChartType.LINE -> {
                if (type.isSleep) "${goal.goalValue}시간 수면하기"
                else "${type.title} ${goal.goalValue}시간"
            }
            GoalChartType.BAR -> "${type.title} ${goal.goalValue}회 이상"
        }
    }

    private fun extractBaseTitle(name: String, suffixUnit: String): String {
        if (name.isBlank()) return "목표"
        val removedUnit = name.substringBefore(" $suffixUnit")
        return removedUnit.substringBeforeLast(" ").ifBlank { name }
    }

    private fun renderSleepChart(
        chart: LineChart,
        values: List<Float>,
        xLabels: List<String>,
        goalY: Float,
        markerSubText: String
    ) {
        val entries = values.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val yValues = entries.map { it.y }
        val yMin = floor(min(yValues.minOrNull() ?: 0f, goalY) - 1f).coerceAtLeast(0f)
        val yMax = ceil(max(yValues.maxOrNull() ?: 0f, goalY) + 1f)

        ChartStyle.applySleep(
            chart = chart,
            xLabels = xLabels,
            goalY = goalY,
            yMin = yMin,
            yMax = yMax
        )

        val mainSet = LineDataSet(entries, "").apply {
            color = ChartStyle.goalColor()
            lineWidth = 3.2f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(false)
            mode = LineDataSet.Mode.LINEAR
            setDrawHighlightIndicators(false)
            highLightColor = ChartStyle.goalColor()
        }

        val lastEntry = entries.last()
        val pointSet = LineDataSet(listOf(lastEntry), "").apply {
            color = ChartStyle.goalColor()
            lineWidth = 0f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(true)
            circleRadius = 7f
            circleHoleRadius = 4f
            setCircleColor(ChartStyle.goalColor())
            circleHoleColor = ChartStyle.circleHoleColor()
            setDrawHighlightIndicators(false)
        }

        chart.data = LineData(mainSet, pointSet)
        chart.marker = SleepMarkerView(requireContext(), markerSubText)
        chart.highlightValue(lastEntry.x, lastEntry.y, 1)
        chart.invalidate()
    }

    private fun renderGenericLineChart(
        chart: LineChart,
        values: List<Float>,
        xLabels: List<String>,
        goalY: Float
    ) {
        val entries = values.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val yValues = entries.map { it.y }
        val yMin = floor(min(yValues.minOrNull() ?: 0f, goalY) - 1f).coerceAtLeast(0f)
        val yMax = ceil(max(yValues.maxOrNull() ?: 0f, goalY) + 1f)

        ChartStyle.applySleep(
            chart = chart,
            xLabels = xLabels,
            goalY = goalY,
            yMin = yMin,
            yMax = yMax
        )

        val dataSet = LineDataSet(entries, "").apply {
            color = ChartStyle.goalColor()
            lineWidth = 2.5f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(true)
            circleRadius = 4f
            circleHoleRadius = 2f
            setCircleColor(ChartStyle.goalColor())
            circleHoleColor = ChartStyle.circleHoleColor()
            mode = LineDataSet.Mode.LINEAR
            setDrawHighlightIndicators(false)
        }

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun renderPomodoroChart(
        chart: CombinedChart,
        groupValues: List<Float>,
        myValues: List<Float>,
        xLabels: List<String>,
        goalY: Float
    ) {
        val barEntries = groupValues.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value)
        }

        val lineEntries = myValues.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val yMin = 0f
        val yMax = ceil(
            max(
                max(groupValues.maxOrNull() ?: 0f, myValues.maxOrNull() ?: 0f),
                goalY
            ) + 1f
        )

        ChartStyle.applyPomodoro(
            chart = chart,
            xLabels = xLabels,
            goalY = goalY,
            yMin = yMin,
            yMax = yMax
        )

        val barDataSet = BarDataSet(barEntries, "").apply {
            color = ChartStyle.barColor()
            setDrawValues(false)
        }

        val lineDataSet = LineDataSet(lineEntries, "").apply {
            color = ChartStyle.lineColor()
            lineWidth = 3f
            setDrawValues(false)
            setDrawCircles(false)
            setDrawFilled(true)
            fillColor = ChartStyle.fillColor()
            fillAlpha = 100
            mode = LineDataSet.Mode.LINEAR
            setDrawHighlightIndicators(false)
        }

        val lastEntry = lineEntries.last()
        val pointSet = LineDataSet(listOf(lastEntry), "").apply {
            color = ChartStyle.lineColor()
            lineWidth = 0f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(true)
            circleRadius = 6f
            circleHoleRadius = 3f
            setCircleColor(ChartStyle.lineColor())
            circleHoleColor = ChartStyle.circleHoleColor()
            setDrawHighlightIndicators(false)
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.5f
        }

        val combinedData = CombinedData().apply {
            setData(barData)
            setData(LineData(lineDataSet, pointSet))
        }

        chart.data = combinedData
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = groupValues.size - 0.5f
        chart.invalidate()
    }

    private fun buildSequentialLabels(size: Int): List<String> {
        return List(size) { (it + 1).toString() }
    }

    private fun buildMemberLabels(
        userProgress: List<UserProgressItem>,
        fallbackSize: Int
    ): List<String> {
        val labels = userProgress.mapIndexed { index, item ->
            val base = item.userEmail.substringBefore("@")
            base.take(4).ifBlank { (index + 1).toString() }
        }
        return if (labels.isNotEmpty()) labels else List(fallbackSize) { (it + 1).toString() }
    }

    private fun bindHeatmap(items: List<GroupAchievementHeatmapItem>) {
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

        heatmapAdapter = RecentAchieveHeatmapAdapter(
            items = safeItems
        ) { item, anchorView, isNowSelected, _ ->
            if (isNowSelected) {
                showHeatmapTooltip(item, anchorView)
            } else {
                hideHeatmapTooltip(clearSelection = false)
            }
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
        if (clearSelection) {
            heatmapAdapter?.clearSelection()
        }
        tooltipDismissRunnable?.let { layoutHeatmapTooltip.removeCallbacks(it) }
        tooltipDismissRunnable = null
    }

    private fun safeBodyString(body: ResponseBody?): String {
        return runCatching { body?.string().orEmpty() }.getOrDefault("")
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private data class GoalTypeUi(
        val title: String,
        val isSleep: Boolean,
        val chartType: GoalChartType
    )

    private enum class GoalChartType {
        LINE, BAR
    }

    private inner class SleepMarkerView(
        context: Context,
        subText: String
    ) : MarkerView(context, android.R.layout.simple_list_item_1) {

        private val root: LinearLayout
        private val title: TextView
        private val sub: TextView

        init {
            removeAllViews()

            root = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = ChartStyle.makeMarkerBackground()
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            }

            title = TextView(context).apply {
                text = "평균 수면시간"
                setTextColor(Color.BLACK)
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
            }

            sub = TextView(context).apply {
                text = subText
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
            return MPPointF((-width / 2f), (-height - dp(12)).toFloat())
        }

        private fun dp(value: Int): Int {
            return (value * resources.displayMetrics.density).toInt()
        }
    }
}