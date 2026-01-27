package com.example.lifemaster.presentation.group.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.group.model.GroupCreateResponse
import com.example.lifemaster.presentation.group.model.GroupGoalCreateRequest
import com.example.lifemaster.presentation.group.model.GroupGoalResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupCreateFragment : Fragment(R.layout.fragment_group_create) {

    // ✅ 현재는 비공개 고정
    private var isPublicGroup: Boolean = false

    private lateinit var goalAdapter: GoalRowAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // -------------------------
        // 1) 공개/비공개 세그먼트: 비공개 고정 + 클릭 불가
        // -------------------------
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

            // ✅ 비공개(오른쪽) 고정
            lp.startToEnd = guidelineHalf.id
            lp.endToEnd = segmentRoot.id

            btnPublic.setTextColor(requireContext().getColor(R.color.black_30))
            btnPrivate.setTextColor(requireContext().getColor(R.color.white))

            selectedPill.layoutParams = lp
            selectedPill.requestLayout()

            // ✅ 클릭 불가
            btnPublic.isEnabled = false
            btnPrivate.isEnabled = false
            segmentRoot.isEnabled = false
        }

        applySegmentUiFixedPrivate()

        /*
        // (나중에 백엔드에서 공개/비공개 지원되면 주석 해제)
        fun applySegmentUi(public: Boolean) { ... }
        btnPublic.setOnClickListener { ... }
        btnPrivate.setOnClickListener { ... }
        */

        // -------------------------
        // 2) 목표 RecyclerView
        // -------------------------
        val rvGoal = view.findViewById<RecyclerView>(R.id.rv_goal_list)

        goalAdapter = GoalRowAdapter(
            items = mutableListOf(),
            onTypeClick = { anchor, current, onPicked ->
                showTypeDropdown(anchor, current, onPicked)
            },
            onCountClick = { anchor, current, onPicked ->
                showCountDropdown(anchor, current, onPicked)
            },
            onDelete = { pos -> goalAdapter.removeAt(pos) }
        )

        rvGoal.layoutManager = LinearLayoutManager(requireContext())
        rvGoal.adapter = goalAdapter

        // -------------------------
        // 3) "추가" 버튼 -> row 추가
        // -------------------------
        view.findViewById<View>(R.id.btn_goal_add).setOnClickListener {
            goalAdapter.add(GoalRow(type = "뽀모도로", count = "1회 이상"))
        }

        // -------------------------
        // 4) 완료하기 -> 비밀번호 다이얼로그 -> 그룹 생성(Call) -> 목표 추가(suspend)
        // -------------------------
        val etName = view.findViewById<EditText>(R.id.et_group_name)
        val etDesc = view.findViewById<EditText>(R.id.et_group_desc)
        val btnDone = view.findViewById<View>(R.id.btn_done)

        btnDone.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val desc = etDesc.text?.toString()?.trim()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "그룹명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showPrivatePasswordDialog { password ->
                val token = getAuthTokenOrNull()
                if (token == null) {
                    Toast.makeText(requireContext(), "토큰이 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
                    return@showPrivatePasswordDialog
                }

                createGroupThenAddGoals(
                    token = token,
                    name = name,
                    desc = desc,
                    icon = null,
                    statistics = null,
                    password = password,
                    goals = goalAdapter.currentItems()
                )
            }
        }
    }

    // =========================
    // 비공개 비밀번호 다이얼로그 (XML id에 맞춤)
    // ids: etPassword / etPasswordConfirm / btnCancel / btnCreate
    // =========================
    private fun showPrivatePasswordDialog(
        onConfirm: (password: String) -> Unit
    ) {
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

    // =========================
    // 토큰: SharedPreferences에서 가져오기 + Bearer 붙이기
    // =========================
    private fun getAuthTokenOrNull(): String? {
        val raw = TokenProvider.getAccessToken(requireContext()) ?: return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    // =========================
    // 그룹 생성(Call) -> 성공하면 목표 추가(suspend Response)
    // =========================
    private fun createGroupThenAddGoals(
        token: String,
        name: String,
        desc: String?,
        icon: String?,
        statistics: List<Int>?,
        password: String?,
        goals: List<GoalRow>
    ) {
        RetrofitInstance.networkService.createGroup(
            token = token,
            name = name,
            description = desc,
            icon = icon,
            statistics = statistics,
            password = password
        ).enqueue(object : Callback<GroupCreateResponse> {

            override fun onResponse(
                call: Call<GroupCreateResponse>,
                response: Response<GroupCreateResponse>
            ) {
                if (!response.isSuccessful) {
                    val err = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "그룹 생성 실패: ${response.code()}\n${err ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val created = response.body()
                if (created == null) {
                    Toast.makeText(requireContext(), "그룹 생성 응답이 비어있습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                val groupId = created.id

                // 목표 없으면 종료
                if (goals.isEmpty()) {
                    Toast.makeText(requireContext(), "그룹 생성 완료", Toast.LENGTH_SHORT).show()
                    // TODO: 이동
                    return
                }

                // ✅ 목표는 suspend API라서 coroutine에서 순차 추가
                lifecycleScope.launch {
                    val ok = addGoalsSequentiallySuspend(token, groupId, goals)
                    if (ok) {
                        Toast.makeText(requireContext(), "그룹/목표 생성 완료", Toast.LENGTH_SHORT).show()
                        // TODO: 이동
                    }
                }
            }

            override fun onFailure(call: Call<GroupCreateResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private suspend fun addGoalsSequentiallySuspend(
        token: String,
        groupId: Long,
        goals: List<GoalRow>
    ): Boolean = withContext(Dispatchers.IO) {
        for (g in goals) {
            val body = GroupGoalCreateRequest(
                name = g.type,
                goalCondition = g.count
            )

            val resp: Response<GroupGoalResponse> =
                RetrofitInstance.networkService.addGoalToGroup(
                    token = token,
                    groupId = groupId,
                    body = body
                )

            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "목표 추가 실패: ${resp.code()}\n${err ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@withContext false
            }
        }
        return@withContext true
    }

    // =========================
    // 드롭다운(횟수): 바로 아래 붙게
    // =========================
    private fun showCountDropdown(
        anchor: View,
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

        bind(R.id.opt_1, "1회 이상")
        bind(R.id.opt_2, "2회 이상")
        bind(R.id.opt_3, "3회 이상")
        bind(R.id.opt_4, "4회 이상")
        bind(R.id.opt_5, "5회 이상")
        bind(R.id.opt_6, "6회 이상")
        bind(R.id.opt_7, "7회 이상")
        bind(R.id.opt_8, "8회 이상")
        bind(R.id.opt_9, "9회 이상")
        bind(R.id.opt_10, "10회 이상")

        popup.showAsDropDown(anchor, 0, 0, Gravity.START)
    }

    // =========================
    // 드롭다운(타입): 바로 아래 붙게
    // =========================
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

    // =========================
    // Data + Adapter
    // =========================
    data class GoalRow(var type: String, var count: String)

    private class GoalRowAdapter(
        private val items: MutableList<GoalRow>,
        private val onTypeClick: (anchor: View, current: String, onPicked: (String) -> Unit) -> Unit,
        private val onCountClick: (anchor: View, current: String, onPicked: (String) -> Unit) -> Unit,
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
            private val onCountClick: (anchor: View, current: String, onPicked: (String) -> Unit) -> Unit,
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
                        row.type = picked
                        tvType.text = picked
                    }
                }

                boxCount.setOnClickListener {
                    onCountClick(boxCount, row.count) { picked ->
                        row.count = picked
                        tvCount.text = picked
                    }
                }

                btnDelete.setOnClickListener { onDelete(pos) }
            }
        }
    }
}