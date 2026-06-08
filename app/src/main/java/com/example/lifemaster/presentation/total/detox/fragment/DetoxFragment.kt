package com.example.lifemaster.presentation.total.detox.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
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
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.total.detox.DetoxRepeatLockLocalManager
import com.example.lifemaster.presentation.total.detox.adapter.DetoxPermanentLockAdapter
import com.example.lifemaster.presentation.total.detox.adapter.DetoxRepeatLockAdapter
import com.example.lifemaster.presentation.total.detox.adapter.DetoxTimeLockAdapter
import com.example.lifemaster.presentation.total.detox.dialog.DetoxPermanentLockServiceDialog
import com.example.lifemaster.presentation.total.detox.dialog.DetoxRepeatLockSettingDialog
import com.example.lifemaster.presentation.total.detox.dialog.DetoxTimeLockDialog
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import com.example.lifemaster.presentation.total.detox.model.RepeatUsageRequest
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxCommonViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DetoxFragment : Fragment(R.layout.fragment_detox) {

    @Inject
    lateinit var networkService: NetworkService

    lateinit var binding: FragmentDetoxBinding

    private val detoxCommonViewModel: DetoxCommonViewModel by activityViewModels()
    private val detoxViewModel: DetoxViewModel by activityViewModels()
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

    private var totalAccumulatedAppUsageTimes: Long = 0L
    private var permanentLockedPackageNames: Set<String>? = null

    private var isSyncingRepeatUsage = false
    private val lastSyncedUsageMap = mutableMapOf<Long, Int>()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetoxBinding.bind(view)

        initViews()
        initListeners()
        initObservers()
        fetchData()
    }

    override fun onResume() {
        super.onResume()
        detoxRepeatLockViewModel.fetchRepeatLockItems()
    }

    private fun fetchData() {
        detoxViewModel.fetchPermanentLockItems()
        detoxViewModel.fetchTimeLockItems()
        detoxRepeatLockViewModel.fetchRepeatLockItems()
    }

    private fun initViews() = with(binding) {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_repeat_lock -> {
                    llRepeatLock.visibility = View.VISIBLE
                    llTimeLock.visibility = View.GONE
                }

                R.id.rb_time_lock -> {
                    llTimeLock.visibility = View.VISIBLE
                    llRepeatLock.visibility = View.GONE
                }
            }
        }

        recyclerviewPermanentLock.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerviewPermanentLock.adapter = permanentLockAdapter

        recyclerviewRepeatLock.layoutManager = LinearLayoutManager(context)
        recyclerviewRepeatLock.adapter = repeatLockAdapter

        recyclerviewTimeLock.adapter = detoxTimeLockAdapter
    }

    private fun initListeners() = with(binding) {
        root.setOnClickListener {
            repeatLockAdapter.closeOpenedItem()
        }

        etSearchApp.setOnClickListener {
            repeatLockAdapter.closeOpenedItem()
        }

        btnAddRepeatLockApp.setOnClickListener {
            repeatLockAdapter.closeOpenedItem()
        }

        btnEditPermanentLockService.setOnClickListener {
            val dialog = DetoxPermanentLockServiceDialog(permanentLockedPackageNames)
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxPermanentLockServiceDialog.TAG)
        }

        btnAddRepeatLockApp.setOnClickListener {
            val dialog = DetoxRepeatLockSettingDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxRepeatLockSettingDialog.TAG)
        }

        etSearchApp.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchAppName = etSearchApp.text.toString()

                if (searchAppName.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "입력 값이 올바르지 않습니다!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnEditorActionListener false
                }

                val currentListApps = detoxRepeatLockViewModel.repeatLockApp.value
                val searchApp = currentListApps?.find { it.appName == searchAppName }

                if (searchApp == null) {
                    Toast.makeText(
                        requireContext(),
                        "해당 앱은 존재하지 않습니다!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    repeatLockAdapter.submitList(listOf(searchApp))
                }

                false
            } else {
                true
            }
        }

        btnTimeLockSetting.setOnClickListener {
            val dialog = DetoxTimeLockDialog().apply {
                isCancelable = false
            }
            dialog.show(childFragmentManager, DetoxTimeLockDialog.TAG)
        }
    }

    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    detoxViewModel.permanentLockItems
                        .combine(detoxViewModel.installedApps) { resource, allApps ->
                            resource to allApps
                        }
                        .collect { (dataResource, allApps) ->
                            when (dataResource) {
                                is DataResource.Success<List<String>> -> {
                                    permanentLockedPackageNames = dataResource.data.toSet()

                                    val filteredApps = allApps.filter { app ->
                                        permanentLockedPackageNames?.contains(app.appPackageName) == true
                                    }

                                    if (filteredApps.isNotEmpty()) {
                                        tvPermanentLockPlaceholder.isVisible = false
                                        recyclerviewPermanentLock.isVisible = true
                                        permanentLockAdapter.submitList(filteredApps.toList())
                                    } else {
                                        tvPermanentLockPlaceholder.isVisible = true
                                        recyclerviewPermanentLock.isVisible = false
                                    }
                                }

                                is DataResource.Error -> Unit
                                DataResource.Idle -> Unit
                                DataResource.Loading -> Unit
                            }
                        }
                }

                launch {
                    detoxViewModel.timeLockItems
                        .combine(detoxViewModel.installedApps) { resource, allApps ->
                            resource to allApps
                        }
                        .collect { (dataResource, allApps) ->
                            when (dataResource) {
                                is DataResource.Success -> {
                                    val timeLockItems = dataResource.data
                                    val activeTimeLockItems = timeLockItems.filter {
                                        !it.disabledToday
                                    }

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

                                    val intent =
                                        Intent("com.example.lifemaster.BROADCAST_RECEIVER").apply {
                                            putStringArrayListExtra(
                                                "TIME_BLOCK_SERVICE_APPLICATIONS",
                                                timeLockedPackages
                                            )
                                            putStringArrayListExtra(
                                                "TIME_BLOCK_SERVICE_INFOS",
                                                timeLockInfos
                                            )
                                        }

                                    LocalBroadcastManager
                                        .getInstance(requireContext())
                                        .sendBroadcast(intent)

                                    if (timeLockItems.isNotEmpty()) {
                                        recyclerviewTimeLock.visibility = View.VISIBLE
                                        tvTimeLockListEmpty.visibility = View.GONE
                                        detoxTimeLockAdapter.allAppList = allApps
                                        detoxTimeLockAdapter.submitList(timeLockItems.toList())
                                    } else {
                                        recyclerviewTimeLock.visibility = View.GONE
                                        tvTimeLockListEmpty.visibility = View.VISIBLE
                                    }
                                }

                                is DataResource.Error -> Unit
                                DataResource.Idle -> Unit
                                DataResource.Loading -> Unit
                            }
                        }
                }

                launch {
                    detoxViewModel.deleteTimeLockResult.collect { dataResource ->
                        if (dataResource is DataResource.Success) {
                            detoxViewModel.fetchTimeLockItems()
                        }
                    }
                }
            }
        }

        detoxCommonViewModel.totalAccumulatedAppUsageTimes.observe(viewLifecycleOwner) { updatedTime ->
            this@DetoxFragment.totalAccumulatedAppUsageTimes = updatedTime
        }

        detoxCommonViewModel.tempElapsedForegroundTime.observe(viewLifecycleOwner) { elapsedForegroundTime ->
            tvAccumulatedTimeOfDay.text =
                convertLongFormat(
                    this@DetoxFragment.totalAccumulatedAppUsageTimes + elapsedForegroundTime
                )
        }

        detoxRepeatLockViewModel.blockServices.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                recyclerviewPermanentLock.visibility = View.VISIBLE
                tvPermanentLockPlaceholder.visibility = View.GONE
            } else {
                recyclerviewPermanentLock.visibility = View.GONE
                tvPermanentLockPlaceholder.visibility = View.VISIBLE
            }

            permanentLockAdapter.submitList(it.toList())
        }

        detoxRepeatLockViewModel.repeatLockApp.observe(viewLifecycleOwner) { repeatLockItems ->
            val localUsageList = repeatLockItems.map { item ->
                val todayUsedMinutes = DetoxRepeatLockLocalManager.getTodayUsedMinutes(
                    context = requireContext(),
                    id = item.id,
                    packageName = item.appPackageName
                )

                Log.d(
                    "RepeatUsage",
                    "LOCAL today usage id=${item.id}, package=${item.appPackageName}, todayUsedMinutes=$todayUsedMinutes"
                )

                val disabledToday = DetoxRepeatLockLocalManager.isDisabledToday(
                    context = requireContext(),
                    id = item.id
                )

                item.copy(
                    accumulatedTime = todayUsedMinutes * 60L * 1000L,
                    disabledToday = disabledToday
                )
            }

            bindRepeatLockList(localUsageList)
            broadcastRepeatLockInfos(localUsageList)
            syncRepeatUsageToServer(localUsageList)
        }
    }

    private fun bindRepeatLockList(items: List<DetoxRepeatLockItem>) = with(binding) {
        if (items.isNotEmpty()) {
            recyclerviewRepeatLock.visibility = View.VISIBLE
            llRepeatLockListEmpty.visibility = View.GONE
        } else {
            recyclerviewRepeatLock.visibility = View.GONE
            llRepeatLockListEmpty.visibility = View.VISIBLE
        }

        repeatLockAdapter.submitList(items.toList())
    }

    private fun broadcastRepeatLockInfos(items: List<DetoxRepeatLockItem>) {
        DetoxRepeatLockLocalManager.saveRepeatBlockInfos(
            context = requireContext(),
            infos = items.map { item ->
                DetoxRepeatLockLocalManager.RepeatBlockInfo(
                    id = item.id,
                    packageName = item.appPackageName,
                    sessionUsageLimit = item.useTime,
                    lockDuration = item.lockTime,
                    dailyMaxUsageLimit = item.maxTime
                )
            }
        )

        val repeatLockInfos = ArrayList(
            items.map { item ->
                "${item.id}|${item.appPackageName}|${item.useTime}|${item.lockTime}|${item.maxTime}"
            }
        )

        val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER").apply {
            putStringArrayListExtra("REPEAT_BLOCK_SERVICE_INFOS", repeatLockInfos)
        }

        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun syncRepeatUsageToServer(items: List<DetoxRepeatLockItem>) {
        if (isSyncingRepeatUsage) return

        val syncTargets = items.mapNotNull { item ->
            val todayUsedMinutes = (item.accumulatedTime / 1000L / 60L).toInt()
            val lastSyncedMinutes = lastSyncedUsageMap[item.id]

            if (lastSyncedMinutes == todayUsedMinutes) {
                null
            } else {
                item to todayUsedMinutes
            }
        }

        if (syncTargets.isEmpty()) return

        isSyncingRepeatUsage = true

        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenProvider.getBearerToken(requireContext())

            if (token.isNullOrBlank()) {
                isSyncingRepeatUsage = false
                return@launch
            }

            withContext(Dispatchers.IO) {
                syncTargets.forEach { (item, todayUsedMinutes) ->
                    runCatching {
                        networkService.syncRepeatUsage(
                            token = token,
                            id = item.id,
                            request = RepeatUsageRequest(
                                todayUsedMinutes = todayUsedMinutes
                            )
                        )
                    }.onSuccess { response ->
                        if (response.isSuccessful) {
                            lastSyncedUsageMap[item.id] = todayUsedMinutes

                            Log.d(
                                "RepeatUsage",
                                "PATCH success id=${item.id}, todayUsedMinutes=$todayUsedMinutes"
                            )
                        } else {
                            Log.e(
                                "RepeatUsage",
                                "PATCH failed id=${item.id}, code=${response.code()}, body=${response.errorBody()?.string()}"
                            )
                        }
                    }.onFailure { throwable ->
                        Log.e(
                            "RepeatUsage",
                            "PATCH error id=${item.id}",
                            throwable
                        )
                    }
                }
            }

            isSyncingRepeatUsage = false
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
                    val holder =
                        viewHolder as DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val limitedDx = dX.coerceIn(-revealWidth, 0f)
                        holder.binding.swipeForeground.translationX = limitedDx
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    val holder =
                        viewHolder as DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder
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

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    val holder =
                        viewHolder as DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder

                    holder.binding.swipeForeground.animate()
                        .translationX(-revealWidth)
                        .setDuration(150)
                        .start()

                    repeatLockAdapter.notifyItemChanged(
                        viewHolder.bindingAdapterPosition
                    )
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