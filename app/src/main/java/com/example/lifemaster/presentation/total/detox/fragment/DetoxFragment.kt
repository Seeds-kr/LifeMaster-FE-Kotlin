package com.example.lifemaster.presentation.total.detox.fragment

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentDetoxBinding
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.total.detox.adapter.DetoxRepeatLockAdapter
import com.example.lifemaster.presentation.total.detox.adapter.DetoxServiceMainAdapter
import com.example.lifemaster.presentation.total.detox.adapter.DetoxTimeLockAdapter
import com.example.lifemaster.presentation.total.detox.dialog.DetoxRepeatLockBlockServiceDialog
import com.example.lifemaster.presentation.total.detox.dialog.DetoxRepeatLockSettingDialog
import com.example.lifemaster.presentation.total.detox.dialog.DetoxTimeLockAllowServiceDialog
import com.example.lifemaster.presentation.total.detox.dialog.DetoxTimeLockDialog
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxCommonViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxTimeLockViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DetoxFragment : Fragment(R.layout.fragment_detox) {

    lateinit var binding: FragmentDetoxBinding
    private val detoxCommonViewModel: DetoxCommonViewModel by activityViewModels()
    private val detoxTimeLockViewModel: DetoxTimeLockViewModel by activityViewModels()
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

    private var totalAccumulatedAppUsageTimes: Long =
        0L // 앱의 총 누적 사용 시간(lifemaster 앱의 현재 포그라운드 상태에서의 누적된 시간 제외)

    private val detoxTimeLockAdapter by lazy {
        DetoxTimeLockAdapter { deleteId ->
            detoxTimeLockViewModel.deleteTimeLockItem(deleteId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetoxBinding.bind(view)
        fetchData()
        initViews()
        initListeners()
        initObservers()
    }

    private fun fetchData() {
        detoxTimeLockViewModel.fetchTimeLockItems() // 시간 잠금 리스트 항목 가져오기
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

        // 반복 잠금 - 차단할 서비스 설정
        binding.recyclerviewBlockService.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewBlockService.adapter = DetoxServiceMainAdapter()

        // 반복 잠금 - 아이템 리스트
        binding.recyclerviewRepeatLock.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewRepeatLock.adapter = DetoxRepeatLockAdapter()

        // 시간 잠금 - 리스트 관련 뷰
        binding.recyclerviewTimeLock.adapter = detoxTimeLockAdapter

    }

    private fun initListeners() {

        // 반복 잠금 - 차단할 서비스 편집
        binding.btnEditRepeatLockBlockService.setOnClickListener {
            val dialog = DetoxRepeatLockBlockServiceDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxRepeatLockBlockServiceDialog.TAG)
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
                            (binding.recyclerviewRepeatLock.adapter as DetoxRepeatLockAdapter).submitList(
                                singleValueList.toList()
                            )
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
    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    detoxTimeLockViewModel.timeLockItems.combine(detoxTimeLockViewModel.repeatLockTargetApplications) { resource, allApps -> resource to allApps }
                        .collect { (dataResource, allApps) ->
                            when (dataResource) {
                                is DataResource.Success -> {
                                    val timeLockItems = dataResource.data
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
                    detoxTimeLockViewModel.deleteTimeLockResult.collect { dataResource ->
                        if(dataResource is DataResource.Success) {
                            detoxTimeLockViewModel.fetchTimeLockItems()
                        }
                    }
                }
            }
        }

        // lifemaster 앱이 포그라운드에 있을 때의 시간을 제외한 총 사용 누적 시간 -> 변수만 변경
        detoxCommonViewModel.totalAccumulatedAppUsageTimes.observe(viewLifecycleOwner) { updatedTime ->
            this.totalAccumulatedAppUsageTimes = updatedTime
        }

        // lifemaster 앱이 포그라운드에 있을 때의 시간을 포함한 총 사용 누적 시간 -> UI 실시간 업데이트
        detoxCommonViewModel.tempElapsedForegroundTime.observe(viewLifecycleOwner) { elapsedForegroundTime ->
            binding.tvAccumulatedTimeOfDay.text =
                convertLongFormat(totalAccumulatedAppUsageTimes + elapsedForegroundTime)
        }

        detoxRepeatLockViewModel.blockServices.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.recyclerviewBlockService.visibility = View.VISIBLE
                binding.tvBlockServiceEmpty.visibility = View.GONE
            } else {
                binding.recyclerviewBlockService.visibility = View.GONE
                binding.tvBlockServiceEmpty.visibility = View.VISIBLE
            }
            (binding.recyclerviewBlockService.adapter as DetoxServiceMainAdapter).updateItems(it)
        }

        detoxRepeatLockViewModel.repeatLockApp.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.recyclerviewRepeatLock.visibility = View.VISIBLE
                binding.llRepeatLockListEmpty.visibility = View.GONE
            } else {
                binding.recyclerviewRepeatLock.visibility = View.GONE
                binding.llRepeatLockListEmpty.visibility = View.VISIBLE
            }
            (binding.recyclerviewRepeatLock.adapter as DetoxRepeatLockAdapter).submitList(it.toList())
        }
    }

    private fun convertLongFormat(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val remainSeconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainSeconds)
    }
}