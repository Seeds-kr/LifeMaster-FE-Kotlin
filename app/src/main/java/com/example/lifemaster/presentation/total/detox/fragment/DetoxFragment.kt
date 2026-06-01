package com.example.lifemaster.presentation.total.detox.fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentDetoxBinding
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.total.detox.adapter.DetoxPermanentLockAdapter
import com.example.lifemaster.presentation.total.detox.adapter.DetoxRepeatLockAdapter
import com.example.lifemaster.presentation.total.detox.adapter.DetoxTimeLockAdapter
import com.example.lifemaster.presentation.total.detox.dialog.DetoxPermanentLockServiceDialog
import com.example.lifemaster.presentation.total.detox.dialog.DetoxRepeatLockSettingDialog
import com.example.lifemaster.presentation.total.detox.dialog.DetoxTimeLockDialog
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxCommonViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DetoxFragment : Fragment(R.layout.fragment_detox) {

    lateinit var binding: FragmentDetoxBinding
    private val detoxCommonViewModel: DetoxCommonViewModel by activityViewModels()
    private val detoxViewModel: DetoxViewModel by activityViewModels()
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

    private var totalAccumulatedAppUsageTimes: Long =
        0L // 앱의 총 누적 사용 시간(lifemaster 앱의 현재 포그라운드 상태에서의 누적된 시간 제외)

    private val permanentLockAdapter by lazy {
        DetoxPermanentLockAdapter()
    }

    private val repeatLockAdapter by lazy {
        DetoxRepeatLockAdapter { item ->
            AlertDialog.Builder(requireContext())
                .setMessage("반복 잠금 설정을 삭제하시겠습니까?")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제") { _, _ ->
                    detoxRepeatLockViewModel.deleteRepeatLockItem(item.id)
                }
                .show()
        }
    }

    private val detoxTimeLockAdapter by lazy {
        DetoxTimeLockAdapter { deleteId ->
            detoxViewModel.deleteTimeLockItem(deleteId)
        }
    }

    private var permanentLockedPackageNames: Set<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetoxBinding.bind(view)
        fetchData()
        initViews()
        initListeners()
        initObservers()
    }

    private fun fetchData() {
        detoxViewModel.fetchPermanentLockItems() // 영구 잠금 리스트 항목 가져오기
        detoxViewModel.fetchTimeLockItems() // 시간 잠금 리스트 항목 가져오기
        detoxRepeatLockViewModel.fetchRepeatLockItems() // 반복 잠금 리스트 항목 가져오기
    }

    private fun initViews() {
        // 공통 - 라디오 버튼 관련 ui 뷰
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_repeat_lock -> {
                    binding.llRepeatLock.visibility = View.VISIBLE
                    binding.llTimeLock.visibility = View.GONE
                }

                R.id.rb_time_lock -> {
                    binding.llTimeLock.visibility = View.VISIBLE
                    binding.llRepeatLock.visibility = View.GONE
                }
            }
        }

        // 영구 잠금
        binding.recyclerviewPermanentLock.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewPermanentLock.adapter = permanentLockAdapter

        // 반복 잠금 - 아이템 리스트
        binding.recyclerviewRepeatLock.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewRepeatLock.adapter = repeatLockAdapter

        // 시간 잠금 - 리스트 관련 뷰
        binding.recyclerviewTimeLock.adapter = detoxTimeLockAdapter

    }

    private fun initListeners() {

        binding.root.setOnClickListener {
            repeatLockAdapter.closeOpenedItem()
        }

        binding.etSearchApp.setOnClickListener {
            repeatLockAdapter.closeOpenedItem()
        }

        binding.btnAddRepeatLockApp.setOnClickListener {
            repeatLockAdapter.closeOpenedItem()
        }

        // 영구 차단할 앱 편집
        binding.btnEditPermanentLockService.setOnClickListener {
            val dialog = DetoxPermanentLockServiceDialog(permanentLockedPackageNames)
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxPermanentLockServiceDialog.TAG)
        }

        // 반복 잠금 - 잠금 앱 추가
        binding.btnAddRepeatLockApp.setOnClickListener {
            val dialog = DetoxRepeatLockSettingDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxRepeatLockSettingDialog.TAG)
        }

        // 반복 잠금 - 검색 기능
        binding.etSearchApp.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val searchAppName = binding.etSearchApp.text.toString()
                    if (searchAppName.isBlank()) {
                        Toast.makeText(requireContext(), "입력 값이 올바르지 않습니다!", Toast.LENGTH_SHORT)
                            .show()
                        return false
                    } else {
                        val currentListApps = detoxRepeatLockViewModel.repeatLockApp.value
                        val searchApp = currentListApps?.find { it.appName == searchAppName }
                        if (searchApp == null) {
                            Toast.makeText(requireContext(), "해당 앱은 존재하지 않습니다!", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val singleValueList = arrayListOf(searchApp)
                            repeatLockAdapter.submitList(singleValueList.toList())
                        }
                        return false // 왜 키보드가 안내려가지?
                    }
                } else {
                    return true
                }
            }
        })

        // 시간 잠금 - 시간 잠금 설정
        binding.btnTimeLockSetting.setOnClickListener {
            val dialog = DetoxTimeLockDialog().apply { isCancelable = false }
            dialog.show(childFragmentManager, DetoxTimeLockDialog.TAG)
        }
    }

    // 공통 - 오늘 사용한 앱의 총 누적 시간
    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    detoxViewModel.permanentLockItems.combine(detoxViewModel.installedApps) { resource, allApps -> resource to allApps }
                        .collect { (dataResource, allApps) ->
                            when(dataResource) {
                                is DataResource.Success<List<String>> -> {
                                    permanentLockedPackageNames = dataResource.data.toSet()
                                    val filteredApps = allApps.filter { app ->
                                        permanentLockedPackageNames!!.contains(app.appPackageName)
                                    }
                                    if(filteredApps.isNotEmpty()) {
                                        tvPermanentLockPlaceholder.isVisible = false
                                        recyclerviewPermanentLock.isVisible = true
                                        permanentLockAdapter.submitList(filteredApps.toList())
                                    } else {
                                        tvPermanentLockPlaceholder.isVisible = true
                                        recyclerviewPermanentLock.isVisible = false
                                    }
                                }
                                is DataResource.Error -> {}
                                DataResource.Idle -> {}
                                DataResource.Loading -> {}
                            }
                        }
                }
                launch {
                    detoxViewModel.timeLockItems.combine(detoxViewModel.installedApps) { resource, allApps -> resource to allApps }
                        .collect { (dataResource, allApps) ->
                            when (dataResource) {
                                is DataResource.Success -> {
                                    val timeLockItems = dataResource.data

                                    val activeTimeLockItems = timeLockItems.filter { !it.disabledToday }

                                    val timeLockedPackages = ArrayList(
                                        activeTimeLockItems
                                            .map { it.lockedAppPackageName }
                                            .filter { it.isNotBlank() }
                                    )

                                    val timeLockInfos = ArrayList(
                                        activeTimeLockItems
                                            .map { item ->
                                                "${item.lockedAppPackageName}|${item.startTime}|${item.endTime}"
                                            }
                                            .filter { it.isNotBlank() }
                                    )

                                    val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER").apply {
                                        putStringArrayListExtra("TIME_BLOCK_SERVICE_APPLICATIONS", timeLockedPackages)
                                        putStringArrayListExtra("TIME_BLOCK_SERVICE_INFOS", timeLockInfos)
                                    }

                                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

                                    if (timeLockItems.isNotEmpty()) {
                                        binding.recyclerviewTimeLock.visibility = View.VISIBLE
                                        binding.tvTimeLockListEmpty.visibility = View.GONE
                                        detoxTimeLockAdapter.allAppList = allApps
                                        detoxTimeLockAdapter.submitList(timeLockItems.toList())
                                    } else {
                                        binding.recyclerviewTimeLock.visibility = View.GONE
                                        binding.tvTimeLockListEmpty.visibility = View.VISIBLE
                                    }
                                }
                                is DataResource.Error -> {}
                                DataResource.Idle -> {}
                                DataResource.Loading -> {}
                            }

                        }
                }
                launch {
                    detoxViewModel.deleteTimeLockResult.collect { dataResource ->
                        if(dataResource is DataResource.Success) {
                            detoxViewModel.fetchTimeLockItems()
                        }
                    }
                }
            }
        }

        // lifemaster 앱이 포그라운드에 있을 때의 시간을 제외한 총 사용 누적 시간 -> 변수만 변경
        detoxCommonViewModel.totalAccumulatedAppUsageTimes.observe(viewLifecycleOwner) { updatedTime ->
            this@DetoxFragment.totalAccumulatedAppUsageTimes = updatedTime
        }

        // lifemaster 앱이 포그라운드에 있을 때의 시간을 포함한 총 사용 누적 시간 -> UI 실시간 업데이트
        detoxCommonViewModel.tempElapsedForegroundTime.observe(viewLifecycleOwner) { elapsedForegroundTime ->
            binding.tvAccumulatedTimeOfDay.text =
                convertLongFormat(this@DetoxFragment.totalAccumulatedAppUsageTimes + elapsedForegroundTime)
        }

        detoxRepeatLockViewModel.blockServices.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.recyclerviewPermanentLock.visibility = View.VISIBLE
                binding.tvPermanentLockPlaceholder.visibility = View.GONE
            } else {
                binding.recyclerviewPermanentLock.visibility = View.GONE
                binding.tvPermanentLockPlaceholder.visibility = View.VISIBLE
            }
            permanentLockAdapter.submitList(it.toList())
        }

        detoxRepeatLockViewModel.repeatLockApp.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.recyclerviewRepeatLock.visibility = View.VISIBLE
                binding.llRepeatLockListEmpty.visibility = View.GONE
            } else {
                binding.recyclerviewRepeatLock.visibility = View.GONE
                binding.llRepeatLockListEmpty.visibility = View.VISIBLE
            }
            repeatLockAdapter.submitList(it.toList())

            val repeatLockInfos = ArrayList(
                it.map { item ->
                    "${item.id}|${item.appPackageName}|${item.useTime}|${item.lockTime}|${item.maxTime}"
                }
            )

            val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER").apply {
                putStringArrayListExtra("REPEAT_BLOCK_SERVICE_INFOS", repeatLockInfos)
            }

            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }

    private fun attachRepeatLockSwipeDelete() {
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                private val revealWidth by lazy {
                    dpToPx(72).toFloat()
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onChildDraw(
                    c: android.graphics.Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val holder = viewHolder as DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val limitedDx = dX.coerceIn(-revealWidth, 0f)
                        holder.binding.swipeForeground.translationX = limitedDx
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    val holder = viewHolder as DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder
                    val currentX = holder.binding.swipeForeground.translationX

                    val targetX = if (currentX <= -revealWidth / 2) {
                        -revealWidth
                    } else {
                        0f
                    }

                    holder.binding.swipeForeground.animate()
                        .translationX(targetX)
                        .setDuration(150)
                        .start()
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val holder = viewHolder as DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder

                    holder.binding.swipeForeground.animate()
                        .translationX(-revealWidth)
                        .setDuration(150)
                        .start()

                    repeatLockAdapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
                }

                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                    return 2f
                }

                override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                    return Float.MAX_VALUE
                }

                override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
                    return Float.MAX_VALUE
                }
            }
        )

        itemTouchHelper.attachToRecyclerView(binding.recyclerviewRepeatLock)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun convertLongFormat(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val remainSeconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainSeconds)
    }
}