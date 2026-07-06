package com.example.lifemaster.presentation.group.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.group.model.GroupCreateResponse
import com.example.lifemaster.presentation.group.model.GroupGoalCreateRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class GroupCreateFragment : Fragment(R.layout.fragment_group_create) {

    companion object {
        private const val ACCESS_TYPE_PUBLIC = "PUBLIC"
        private const val ACCESS_TYPE_PASSWORD = "PASSWORD"
        private const val GOAL_CONDITION_COUNT = "COUNT"
        private const val GOAL_CONDITION_TIME = "TIME"
        private const val GOAL_DURATION_DAILY = "DAILY"

        private const val ICON_DEFAULT = "ic_group"
        private const val ICON_PAYLOAD_SEPARATOR = "|"
    }

    private lateinit var goalAdapter: GoalRowAdapter

    @Inject
    lateinit var networkService: NetworkService

    private var isSubmitting = false
    private var selectedAccessType: String = ACCESS_TYPE_PUBLIC
    private var selectedIconKey: String = ICON_DEFAULT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val guidelineHalf = view.findViewById<Guideline>(R.id.guideline_half)
        val selectedPill = view.findViewById<View>(R.id.view_selected_pill)
        val btnPublic = view.findViewById<TextView>(R.id.btn_public)
        val btnPrivate = view.findViewById<TextView>(R.id.btn_private)

        fun applySegmentUi(accessType: String) {
            selectedAccessType = accessType

            val lp = selectedPill.layoutParams as ConstraintLayout.LayoutParams
            lp.startToStart = ConstraintLayout.LayoutParams.UNSET
            lp.endToEnd = ConstraintLayout.LayoutParams.UNSET
            lp.startToEnd = ConstraintLayout.LayoutParams.UNSET
            lp.endToStart = ConstraintLayout.LayoutParams.UNSET

            if (accessType == ACCESS_TYPE_PUBLIC) {
                lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                lp.endToStart = guidelineHalf.id

                btnPublic.setTextColor(requireContext().getColor(R.color.white))
                btnPrivate.setTextColor(requireContext().getColor(R.color.black_30))
            } else {
                lp.startToEnd = guidelineHalf.id
                lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

                btnPublic.setTextColor(requireContext().getColor(R.color.black_30))
                btnPrivate.setTextColor(requireContext().getColor(R.color.white))
            }

            selectedPill.layoutParams = lp
            selectedPill.requestLayout()
        }

        applySegmentUi(ACCESS_TYPE_PUBLIC)

        btnPublic.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            applySegmentUi(ACCESS_TYPE_PUBLIC)
        }

        btnPrivate.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            applySegmentUi(ACCESS_TYPE_PASSWORD)
        }

        val boxIcon = view.findViewById<View>(R.id.box_icon)
        val ivGroupIcon = view.findViewById<ImageView>(R.id.iv_group_icon)

        applySelectedGroupIcon(ivGroupIcon)

        boxIcon.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            showGroupIconPicker(boxIcon, ivGroupIcon)
        }

        val rvGoal = view.findViewById<RecyclerView>(R.id.rv_goal_list)

        goalAdapter = GoalRowAdapter(
            items = mutableListOf(),
            onTypeClick = { anchor, current, onPicked ->
                showTypeDropdown(anchor, current, onPicked)
            },
            onCountClick = { anchor, goalType, current, onPicked ->
                showCountDropdown(anchor, goalType, current, onPicked)
            },
            onDelete = { pos ->
                goalAdapter.removeAt(pos)
            }
        )

        rvGoal.layoutManager = LinearLayoutManager(requireContext())
        rvGoal.adapter = goalAdapter

        view.findViewById<View>(R.id.btn_goal_add).setOnClickListener {
            goalAdapter.add(GoalRow(type = "뽀모도로", count = "1회 이상"))
        }

        val etName = view.findViewById<EditText>(R.id.et_group_name)
        val etDesc = view.findViewById<EditText>(R.id.et_group_desc)
        val btnDone = view.findViewById<View>(R.id.btn_done)

        btnDone.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            val name = etName.text?.toString()?.trim().orEmpty()
            val desc = etDesc.text?.toString()?.trim()
            val goals = goalAdapter.currentItems()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "그룹명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validateGoals(goals)) return@setOnClickListener

            val token = getAuthTokenOrNull()
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val iconPayload = buildGroupIconPayload()

            if (selectedAccessType == ACCESS_TYPE_PUBLIC) {
                createGroupAndGoals(
                    token = token,
                    name = name,
                    desc = desc,
                    icon = iconPayload,
                    statistics = null,
                    password = null,
                    accessType = ACCESS_TYPE_PUBLIC,
                    goals = goals,
                    btnDone = btnDone
                )
            } else {
                showPrivatePasswordDialog { password ->
                    if (isSubmitting) return@showPrivatePasswordDialog

                    createGroupAndGoals(
                        token = token,
                        name = name,
                        desc = desc,
                        icon = iconPayload,
                        statistics = null,
                        password = password,
                        accessType = ACCESS_TYPE_PASSWORD,
                        goals = goals,
                        btnDone = btnDone
                    )
                }
            }
        }
    }

    private fun showGroupIconPicker(anchor: View, ivGroupIcon: ImageView) {
        val context = requireContext()
        lateinit var popup: PopupWindow

        val popupRoot = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.START
            setPadding(14.dp(), 14.dp(), 14.dp(), 14.dp())
            background = createRoundRectDrawable(
                fillColor = Color.WHITE,
                cornerRadius = 18.dp().toFloat(),
                strokeColor = Color.parseColor("#D0D0D0"),
                strokeWidth = 1.dp()
            )
        }

        fun createIconCell(option: GroupIconOption): FrameLayout {
            val cell = FrameLayout(context).apply {
                background = createIconCellDrawable(option.key == selectedIconKey)
                isClickable = true
                isFocusable = true
            }

            val icon = ImageView(context).apply {
                setImageResource(option.resId)
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = false
                setColorFilter(Color.BLACK)
                contentDescription = option.label
            }

            val iconLp = FrameLayout.LayoutParams(22.dp(), 22.dp()).apply {
                gravity = Gravity.CENTER
            }

            cell.addView(icon, iconLp)

            cell.setOnClickListener {
                selectedIconKey = option.key
                applySelectedGroupIcon(ivGroupIcon)
                popup.dismiss()
            }

            return cell
        }

        val options = getGroupIconOptions()

        options.chunked(4).forEach { rowOptions ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.START
            }

            rowOptions.forEach { option ->
                val cell = createIconCell(option)

                val cellLp = LinearLayout.LayoutParams(42.dp(), 42.dp()).apply {
                    marginStart = 3.dp()
                    marginEnd = 3.dp()
                    topMargin = 3.dp()
                    bottomMargin = 3.dp()
                }

                row.addView(cell, cellLp)
            }

            popupRoot.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        popup = PopupWindow(
            popupRoot,
            220.dp(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 18f
        }

        popup.showAsDropDown(anchor, 0, 8.dp(), Gravity.START)
    }

    private fun applySelectedGroupIcon(ivGroupIcon: ImageView) {
        ivGroupIcon.setImageResource(getGroupIconRes(selectedIconKey))
        ivGroupIcon.imageTintList = null
        ivGroupIcon.clearColorFilter()
    }

    private fun buildGroupIconPayload(): String {
        return selectedIconKey
    }

    private data class GroupIconOption(
        val key: String,
        val label: String,
        @DrawableRes val resId: Int
    )

    private fun getGroupIconOptions(): List<GroupIconOption> {
        return listOf(
            GroupIconOption("ic_alarm", "알람", R.drawable.ic_alarm),
            GroupIconOption("ic_book_open", "기록", R.drawable.ic_book_open),
            GroupIconOption("ic_calendar", "일정", R.drawable.ic_calendar),
            GroupIconOption("ic_certificate", "인증", R.drawable.ic_certificate),
            GroupIconOption("ic_chart", "통계", R.drawable.ic_chart),
            GroupIconOption("ic_clock", "시간", R.drawable.ic_clock),
            GroupIconOption("ic_clock_sleep", "수면", R.drawable.ic_clock_sleep),
            GroupIconOption("ic_community", "커뮤니티", R.drawable.ic_community),
            GroupIconOption("ic_group", "그룹", R.drawable.ic_group),
            GroupIconOption("ic_home", "홈", R.drawable.ic_home)
        )
    }

    @DrawableRes
    private fun getGroupIconRes(icon: String?): Int {
        val key = icon.orEmpty()
            .substringBefore(ICON_PAYLOAD_SEPARATOR)
            .ifBlank { ICON_DEFAULT }

        return when (key) {
            "ic_alarm" -> R.drawable.ic_alarm
            "ic_book_open" -> R.drawable.ic_book_open
            "ic_calendar" -> R.drawable.ic_calendar
            "ic_certificate" -> R.drawable.ic_certificate
            "ic_chart" -> R.drawable.ic_chart
            "ic_clock" -> R.drawable.ic_clock
            "ic_clock_sleep" -> R.drawable.ic_clock_sleep
            "ic_community" -> R.drawable.ic_community
            "ic_group" -> R.drawable.ic_group
            "ic_home" -> R.drawable.ic_home
            else -> R.drawable.ic_group
        }
    }

    private fun createIconCellDrawable(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 22.dp().toFloat()
            setColor(
                if (selected) Color.parseColor("#F1F1F1")
                else Color.TRANSPARENT
            )

            if (selected) {
                setStroke(1.dp(), Color.parseColor("#D7D7D7"))
            }
        }
    }

    private fun createRoundRectDrawable(
        fillColor: Int,
        cornerRadius: Float,
        strokeColor: Int? = null,
        strokeWidth: Int = 0
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            this.cornerRadius = cornerRadius

            if (strokeColor != null && strokeWidth > 0) {
                setStroke(strokeWidth, strokeColor)
            }
        }
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun showPrivatePasswordDialog(onConfirm: (password: String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_group_create_private, null)

        val etPw = dialogView.findViewById<EditText>(R.id.etPassword)
        val etPwConfirm = dialogView.findViewById<EditText>(R.id.etPasswordConfirm)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnCreate = dialogView.findViewById<TextView>(R.id.btnCreate)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnCreate.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            val pw = etPw.text?.toString()?.trim().orEmpty()
            val pw2 = etPwConfirm.text?.toString()?.trim().orEmpty()

            if (pw.isBlank() || pw2.isBlank()) {
                Toast.makeText(requireContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw != pw2) {
                Toast.makeText(requireContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            onConfirm(pw)
        }
    }

    private fun validateGoals(goals: List<GoalRow>): Boolean {
        if (goals.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "최소목표를 추가해주세요.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val selectedGoalTypes = mutableSetOf<String>()

        for ((index, goal) in goals.withIndex()) {
            val normalizedType = normalizeGoalType(goal.type)

            if (normalizedType.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "${index + 1}번째 목표 종류를 선택해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (selectedGoalTypes.contains(normalizedType)) {
                Toast.makeText(
                    requireContext(),
                    "동일한 종류의 목표는 추가할 수 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            selectedGoalTypes.add(normalizedType)

            if (parseGoalValue(goal.count) <= 0) {
                Toast.makeText(
                    requireContext(),
                    "${index + 1}번째 목표 값이 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }

        return true
    }

    private fun normalizeGoalType(type: String): String {
        val trimmed = type.trim()

        return when {
            trimmed.contains("수면") -> "SLEEP"
            trimmed.contains("뽀모도로") -> "POMODORO"
            trimmed.contains("디톡스") || trimmed.contains("폰") -> "DETOX"
            trimmed.contains("감사") -> "GRATITUDE"
            trimmed.contains("자아") || trimmed.contains("성찰") -> "REFLECTION"
            trimmed.contains("챌린지") -> "CHALLENGE"
            else -> trimmed.uppercase()
        }
    }

    private fun getAuthTokenOrNull(): String? {
        return TokenProvider.getBearerToken(requireContext())
    }

    private fun createGroupAndGoals(
        token: String,
        name: String,
        desc: String?,
        icon: String?,
        statistics: List<Int>?,
        password: String?,
        accessType: String,
        goals: List<GoalRow>,
        btnDone: View
    ) {
        if (isSubmitting) return

        isSubmitting = true
        btnDone.isEnabled = false

        networkService.createGroup(
            token = token,
            name = name,
            description = desc,
            icon = icon,
            statistics = statistics,
            password = password,
            accessType = accessType
        ).enqueue(object : Callback<GroupCreateResponse> {

            override fun onResponse(
                call: Call<GroupCreateResponse>,
                response: Response<GroupCreateResponse>
            ) {
                if (!isAdded) {
                    isSubmitting = false
                    btnDone.isEnabled = true
                    return
                }

                if (!response.isSuccessful) {
                    isSubmitting = false
                    btnDone.isEnabled = true

                    val err = response.errorBody()?.string()
                    Log.e("GroupCreate", "createGroup fail code=${response.code()} err=$err")
                    Toast.makeText(requireContext(), "그룹 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                val created = response.body()
                if (created == null) {
                    isSubmitting = false
                    btnDone.isEnabled = true
                    Toast.makeText(requireContext(), "그룹 생성 응답이 비어있습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                val groupId = created.id

                lifecycleScope.launch {
                    val goalsOk = if (goals.isEmpty()) {
                        true
                    } else {
                        addGoalsSequentially(token, groupId, goals)
                    }

                    if (!goalsOk) {
                        isSubmitting = false
                        btnDone.isEnabled = true
                        return@launch
                    }

                    val inviteCode = if (accessType == ACCESS_TYPE_PUBLIC) {
                        ""
                    } else {
                        fetchInviteCodeSafely(token, groupId)
                    }

                    val goalLines = ArrayList(goals.map { buildGoalLine(it) })

                    val bundle = Bundle().apply {
                        putLong("groupId", groupId)
                        putString("groupName", created.name ?: name)
                        putString("groupDesc", created.description ?: (desc ?: ""))
                        putString("inviteCode", inviteCode.ifBlank { created.password.orEmpty() })
                        putString("groupAccessType", accessType)
                        putString("groupIcon", created.icon ?: icon.orEmpty())
                        putStringArrayList("goalLines", goalLines)
                    }

                    isSubmitting = false
                    btnDone.isEnabled = true

                    if (isAdded) {
                        findNavController().navigate(
                            R.id.action_groupCreateFragment_to_groupCreateSuccessFragment,
                            bundle
                        )
                    }
                }
            }

            override fun onFailure(call: Call<GroupCreateResponse>, t: Throwable) {
                if (!isAdded) {
                    isSubmitting = false
                    btnDone.isEnabled = true
                    return
                }

                isSubmitting = false
                btnDone.isEnabled = true
                Log.e("GroupCreate", "createGroup network error", t)
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private suspend fun addGoalsSequentially(
        token: String,
        groupId: Long,
        goals: List<GoalRow>
    ): Boolean = withContext(Dispatchers.IO) {

        for ((idx, goal) in goals.withIndex()) {
            val request = buildGoalRequest(goal)

            Log.e(
                "GroupCreate",
                "goal request idx=$idx name=${request.name}, " +
                        "goalType=${request.goalType}, goalCondition=${request.goalCondition}, " +
                        "value=${request.value}, duration=${request.duration}"
            )

            val resp = networkService.addGoalToGroup(
                token = token,
                groupId = groupId,
                name = request.name,
                goalType = request.goalType,
                goalCondition = request.goalCondition,
                value = request.value,
                duration = request.duration
            )

            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e(
                    "GroupCreate",
                    "addGoal fail idx=$idx code=${resp.code()} err=$err " +
                            "name=${request.name}, goalType=${request.goalType}, " +
                            "goalCondition=${request.goalCondition}, value=${request.value}, " +
                            "duration=${request.duration}"
                )

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            getGoalAddErrorMessage(resp.code(), err),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                return@withContext false
            }
        }

        true
    }

    private fun getGoalAddErrorMessage(code: Int, errorBody: String?): String {
        val error = errorBody.orEmpty()

        return when {
            code == 409 -> "동일한 종류의 목표는 추가할 수 없습니다."
            code == 400 && error.contains("duplicate", ignoreCase = true) -> "동일한 종류의 목표는 추가할 수 없습니다."
            code == 400 && error.contains("goalType", ignoreCase = true) -> "목표 타입 정보가 올바르지 않습니다."
            code == 500 && error.contains("duplicate", ignoreCase = true) -> "동일한 종류의 목표는 추가할 수 없습니다."
            code == 500 -> "목표 추가에 실패했습니다. 동일한 종류의 목표가 있는지 확인해주세요."
            else -> "목표 추가에 실패했습니다."
        }
    }

    private fun buildGoalRequest(goal: GoalRow): GroupGoalCreateRequest {
        val value = parseGoalValue(goal.count)
        val goalType = normalizeGoalType(goal.type)

        return when (goalType) {
            "SLEEP" -> GroupGoalCreateRequest(
                name = "수면 ${value}시간",
                goalType = "SLEEP",
                goalCondition = GOAL_CONDITION_TIME,
                value = value,
                duration = GOAL_DURATION_DAILY
            )

            "POMODORO" -> GroupGoalCreateRequest(
                name = "뽀모도로 ${value}회",
                goalType = "POMODORO",
                goalCondition = GOAL_CONDITION_COUNT,
                value = value,
                duration = GOAL_DURATION_DAILY
            )

            "DETOX" -> GroupGoalCreateRequest(
                name = "디톡스 ${value}시간",
                goalType = "DETOX",
                goalCondition = GOAL_CONDITION_TIME,
                value = value,
                duration = GOAL_DURATION_DAILY
            )

            "GRATITUDE" -> GroupGoalCreateRequest(
                name = "감사일기 ${value}회",
                goalType = "GRATITUDE",
                goalCondition = GOAL_CONDITION_COUNT,
                value = value,
                duration = GOAL_DURATION_DAILY
            )

            "REFLECTION" -> GroupGoalCreateRequest(
                name = "자아성찰 ${value}회",
                goalType = "REFLECTION",
                goalCondition = GOAL_CONDITION_COUNT,
                value = value,
                duration = GOAL_DURATION_DAILY
            )

            "CHALLENGE" -> GroupGoalCreateRequest(
                name = "챌린지 ${value}회",
                goalType = "CHALLENGE",
                goalCondition = GOAL_CONDITION_COUNT,
                value = value,
                duration = GOAL_DURATION_DAILY
            )

            else -> GroupGoalCreateRequest(
                name = "${goal.type} ${value}회",
                goalType = goalType,
                goalCondition = GOAL_CONDITION_COUNT,
                value = value,
                duration = GOAL_DURATION_DAILY
            )
        }
    }

    private fun buildGoalLine(goal: GoalRow): String {
        val value = parseGoalValue(goal.count)
        val goalType = normalizeGoalType(goal.type)

        return when (goalType) {
            "SLEEP" -> "수면 ${value}시간"
            "POMODORO" -> "뽀모도로 ${value}회"
            "DETOX" -> "디톡스 ${value}시간"
            "GRATITUDE" -> "감사일기 ${value}회"
            "REFLECTION" -> "자아성찰 ${value}회"
            "CHALLENGE" -> "챌린지 ${value}회"
            else -> "${goal.type} ${value}회"
        }
    }

    private suspend fun fetchInviteCodeSafely(token: String, groupId: Long): String {
        return withContext(Dispatchers.IO) {
            runCatching {
                val resp = networkService.getGroupInviteCode(token, groupId)

                if (!resp.isSuccessful) {
                    Log.e("GroupCreate", "invite fail code=${resp.code()} err=${resp.errorBody()?.string()}")
                    return@runCatching ""
                }

                val raw = resp.body()?.string().orEmpty()

                if (raw.isNotBlank() && !raw.trim().startsWith("{")) {
                    return@runCatching raw.trim()
                }

                val obj = JSONObject(raw)
                when {
                    obj.has("inviteCode") -> obj.getString("inviteCode")
                    obj.has("code") -> obj.getString("code")
                    obj.has("data") -> obj.optString("data")
                    else -> raw
                }.trim()
            }.getOrElse { e ->
                Log.e("GroupCreate", "invite exception", e)
                ""
            }
        }
    }

    private fun parseGoalValue(label: String): Int {
        val digits = label.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 0
    }

    private fun isTimeGoal(goalType: String): Boolean {
        return goalType.contains("수면") ||
                goalType.contains("디톡스") ||
                goalType.contains("폰")
    }

    private fun formatGoalCount(goalType: String, value: Int): String {
        val unit = if (isTimeGoal(goalType)) "시간" else "회"
        return "${value}${unit} 이상"
    }

    private fun showCountDropdown(
        anchor: View,
        goalType: String,
        selectedValue: String,
        onPicked: (String) -> Unit
    ) {
        val popupView = layoutInflater.inflate(R.layout.item_group_goal_option, null)

        val popup = PopupWindow(
            popupView,
            anchor.width,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 16f
        }

        fun bind(id: Int, number: Int) {
            val value = formatGoalCount(goalType, number)
            val tv = popupView.findViewById<TextView>(id)

            tv.text = value
            tv.setTextColor(
                if (value == selectedValue) requireContext().getColor(R.color.purple_100)
                else requireContext().getColor(R.color.black)
            )

            tv.setOnClickListener {
                onPicked(value)
                popup.dismiss()
            }
        }

        bind(R.id.opt_1, 1)
        bind(R.id.opt_2, 2)
        bind(R.id.opt_3, 3)
        bind(R.id.opt_4, 4)
        bind(R.id.opt_5, 5)
        bind(R.id.opt_6, 6)
        bind(R.id.opt_7, 7)
        bind(R.id.opt_8, 8)
        bind(R.id.opt_9, 9)
        bind(R.id.opt_10, 10)

        popup.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    private fun showTypeDropdown(
        anchor: View,
        selectedValue: String,
        onPicked: (String) -> Unit
    ) {
        val popupView = layoutInflater.inflate(R.layout.item_group_goal_type, null)

        val popup = PopupWindow(
            popupView,
            anchor.width,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 16f
        }

        fun bind(id: Int, value: String) {
            val tv = popupView.findViewById<TextView>(id)

            tv.setTextColor(
                if (value == selectedValue) requireContext().getColor(R.color.purple_100)
                else requireContext().getColor(R.color.black)
            )

            tv.setOnClickListener {
                onPicked(value)
                popup.dismiss()
            }
        }

        bind(R.id.type_1, "뽀모도로")
        bind(R.id.type_2, "디톡스")
        bind(R.id.type_3, "챌린지")
        bind(R.id.type_4, "감사일기")
        bind(R.id.type_5, "자아성찰")
        bind(R.id.type_6, "수면")

        popup.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    data class GoalRow(
        var type: String,
        var count: String
    )

    private class GoalRowAdapter(
        private val items: MutableList<GoalRow>,
        private val onTypeClick: (anchor: View, current: String, onPicked: (String) -> Unit) -> Unit,
        private val onCountClick: (
            anchor: View,
            goalType: String,
            current: String,
            onPicked: (String) -> Unit
        ) -> Unit,
        private val onDelete: (pos: Int) -> Unit
    ) : RecyclerView.Adapter<GoalRowAdapter.VH>() {

        fun add(item: GoalRow) {
            items.add(item)
            notifyItemInserted(items.lastIndex)
        }

        fun removeAt(pos: Int) {
            if (pos !in items.indices) return
            items.removeAt(pos)
            notifyItemRemoved(pos)
            notifyItemRangeChanged(pos, items.size - pos)
        }

        fun currentItems(): List<GoalRow> = items.toList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = View.inflate(parent.context, R.layout.dialog_group_goal_setting, null)
            v.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            return VH(v, onTypeClick, onCountClick, onDelete)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position], position)
        }

        override fun getItemCount(): Int = items.size

        class VH(
            itemView: View,
            private val onTypeClick: (anchor: View, current: String, onPicked: (String) -> Unit) -> Unit,
            private val onCountClick: (
                anchor: View,
                goalType: String,
                current: String,
                onPicked: (String) -> Unit
            ) -> Unit,
            private val onDelete: (pos: Int) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private val boxType: View = itemView.findViewById(R.id.box_goal_type)
            private val boxCount: View = itemView.findViewById(R.id.box_goal_count)
            private val tvType: TextView = itemView.findViewById(R.id.tv_goal_type)
            private val tvCount: TextView = itemView.findViewById(R.id.tv_goal_count)
            private val btnDelete: View = itemView.findViewById(R.id.btn_goal_delete)

            fun bind(row: GoalRow, pos: Int) {
                tvType.text = row.type
                tvCount.text = row.count

                boxType.setOnClickListener {
                    onTypeClick(boxType, row.type) { picked ->
                        val currentValue = parseValue(row.count).coerceAtLeast(1)

                        row.type = picked
                        row.count = formatCountByType(picked, currentValue)

                        tvType.text = row.type
                        tvCount.text = row.count
                    }
                }

                boxCount.setOnClickListener {
                    onCountClick(boxCount, row.type, row.count) { picked ->
                        row.count = picked
                        tvCount.text = picked
                    }
                }

                btnDelete.setOnClickListener { onDelete(pos) }
            }

            private fun parseValue(label: String): Int {
                return label.filter { it.isDigit() }.toIntOrNull() ?: 1
            }

            private fun isTimeGoal(type: String): Boolean {
                return type.contains("수면") ||
                        type.contains("디톡스") ||
                        type.contains("폰")
            }

            private fun formatCountByType(type: String, value: Int): String {
                val unit = if (isTimeGoal(type)) "시간" else "회"
                return "${value}${unit} 이상"
            }
        }
    }
}