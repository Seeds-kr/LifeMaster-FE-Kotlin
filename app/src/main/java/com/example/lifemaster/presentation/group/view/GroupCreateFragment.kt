package com.example.lifemaster.presentation.group.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
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
        private const val ACCESS_TYPE_PASSWORD = "PASSWORD"
        private const val GOAL_CONDITION_COUNT = "COUNT"
        private const val GOAL_CONDITION_TIME = "TIME"
        private const val GOAL_DURATION_DAILY = "DAILY"
    }

    private lateinit var goalAdapter: GoalRowAdapter

    @Inject
    lateinit var networkService: NetworkService

    private var isSubmitting = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val segmentRoot = view.findViewById<ConstraintLayout>(R.id.segment_root)
        val guidelineHalf = view.findViewById<Guideline>(R.id.guideline_half)
        val selectedPill = view.findViewById<View>(R.id.view_selected_pill)
        val btnPublic = view.findViewById<TextView>(R.id.btn_public)
        val btnPrivate = view.findViewById<TextView>(R.id.btn_private)

        fun applySegmentUiFixedPrivate() {
            val lp = selectedPill.layoutParams as ConstraintLayout.LayoutParams
            lp.startToStart = ConstraintLayout.LayoutParams.UNSET
            lp.endToEnd = ConstraintLayout.LayoutParams.UNSET
            lp.startToEnd = ConstraintLayout.LayoutParams.UNSET
            lp.endToStart = ConstraintLayout.LayoutParams.UNSET
            lp.startToEnd = guidelineHalf.id
            lp.endToEnd = segmentRoot.id

            btnPublic.setTextColor(requireContext().getColor(R.color.black_30))
            btnPrivate.setTextColor(requireContext().getColor(R.color.white))

            selectedPill.layoutParams = lp
            selectedPill.requestLayout()

            btnPublic.isEnabled = false
            btnPrivate.isEnabled = false
            segmentRoot.isEnabled = false
        }

        applySegmentUiFixedPrivate()

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

            showPrivatePasswordDialog { password ->
                if (isSubmitting) return@showPrivatePasswordDialog

                val token = getAuthTokenOrNull()
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@showPrivatePasswordDialog
                }

                createGroupAndGoals(
                    token = token,
                    name = name,
                    desc = desc,
                    icon = null,
                    statistics = null,
                    password = password,
                    accessType = ACCESS_TYPE_PASSWORD,
                    goals = goals,
                    btnDone = btnDone
                )
            }
        }
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

                    val inviteCode = fetchInviteCodeSafely(token, groupId)
                    val goalLines = ArrayList(goals.map { buildGoalLine(it) })

                    val bundle = Bundle().apply {
                        putLong("groupId", groupId)
                        putString("groupName", created.name ?: name)
                        putString("groupDesc", created.description ?: (desc ?: ""))
                        putString("inviteCode", created.password ?: inviteCode)
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