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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentDetoxBinding
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

class DetoxFragment : Fragment(R.layout.fragment_detox) {

    lateinit var binding: FragmentDetoxBinding
    private val detoxCommonViewModel: DetoxCommonViewModel by activityViewModels()
    private val detoxTimeLockViewModel: DetoxTimeLockViewModel by activityViewModels()
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

    private var totalAccumulatedAppUsageTimes: Long = 0L // 앱의 총 누적 사용 시간(lifemaster 앱의 현재 포그라운드 상태에서의 누적된 시간 제외)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DetoxFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DetoxFragment", "onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DetoxFragment", "onViewCreated")
        binding = FragmentDetoxBinding.bind(view)
        setupViews()
        setupListeners()
        observing()
    }

    private fun setupViews() {
        // 공통 - 라디오 버튼 관련 ui 뷰
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
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
        binding.recyclerviewBlockService.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerviewBlockService.adapter = DetoxServiceMainAdapter()

        // 반복 잠금 - 아이템 리스트
        binding.recyclerviewRepeatLock.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewRepeatLock.adapter = DetoxRepeatLockAdapter()

        // 시간 잠금 - 리스트 관련 뷰
        val detoxTimeLockAdapter = DetoxTimeLockAdapter()
        binding.recyclerviewTimeLock.adapter = detoxTimeLockAdapter

        // 시간 잠금 - 허용할 서비스 설정
        binding.recyclerviewAllowService.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerviewAllowService.adapter = DetoxServiceMainAdapter()
    }

    private fun setupListeners() {

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
        binding.etSearchApp.setOnEditorActionListener(object: OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val searchAppName = binding.etSearchApp.text.toString()
                    if(searchAppName.isBlank()) {
                        Toast.makeText(requireContext(), "입력 값이 올바르지 않습니다!", Toast.LENGTH_SHORT).show()
                        return false
                    }
                    else {
                        val currentListApps = detoxRepeatLockViewModel.repeatLockApp.value
                        val searchApp = currentListApps?.find { it.appName == searchAppName }
                        if(searchApp == null) {
                            Toast.makeText(requireContext(), "해당 앱은 존재하지 않습니다!", Toast.LENGTH_SHORT).show()
                        } else {
                            val singleValueList = arrayListOf(searchApp)
                            (binding.recyclerviewRepeatLock.adapter as DetoxRepeatLockAdapter).submitList(singleValueList.toList())
                        }
                        return false // 왜 키보드가 안내려가지?
                    }
                } else {
                    return true
                }
            }
        })

        // 시간 잠금 - 허용할 서비스 편집
        binding.btnEditTimeLockAllowService.setOnClickListener {
            val dialog = DetoxTimeLockAllowServiceDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxTimeLockAllowServiceDialog.TAG)
        }

        // 시간 잠금 - 시간 잠금 설정
        binding.btnTimeLockSetting.setOnClickListener {
            val dialog = DetoxTimeLockDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxTimeLockDialog.TAG)
        }
    }

    // 공통 - 오늘 사용한 앱의 총 누적 시간
    private fun observing() {

        // lifemaster 앱이 포그라운드에 있을 때의 시간을 제외한 총 사용 누적 시간 -> 변수만 변경
        detoxCommonViewModel.totalAccumulatedAppUsageTimes.observe(viewLifecycleOwner) { updatedTime ->
            this.totalAccumulatedAppUsageTimes = updatedTime
        }

        // lifemaster 앱이 포그라운드에 있을 때의 시간을 포함한 총 사용 누적 시간 -> UI 실시간 업데이트
        detoxCommonViewModel.tempElapsedForegroundTime.observe(viewLifecycleOwner) { elapsedForegroundTime ->
            binding.tvAccumulatedTimeOfDay.text = convertLongFormat(totalAccumulatedAppUsageTimes + elapsedForegroundTime)
        }

        detoxRepeatLockViewModel.blockServices.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                binding.recyclerviewBlockService.visibility = View.VISIBLE
                binding.tvBlockServiceEmpty.visibility = View.GONE
            } else {
                binding.recyclerviewBlockService.visibility = View.GONE
                binding.tvBlockServiceEmpty.visibility = View.VISIBLE
            }
            (binding.recyclerviewBlockService.adapter as DetoxServiceMainAdapter).updateItems(it)
        }

        detoxRepeatLockViewModel.repeatLockApp.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                binding.recyclerviewRepeatLock.visibility = View.VISIBLE
                binding.llRepeatLockListEmpty.visibility = View.GONE
            } else {
                binding.recyclerviewRepeatLock.visibility = View.GONE
                binding.llRepeatLockListEmpty.visibility = View.VISIBLE
            }
            (binding.recyclerviewRepeatLock.adapter as DetoxRepeatLockAdapter).submitList(it.toList())
        }

        detoxTimeLockViewModel.allowServices.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.recyclerviewAllowService.visibility = View.VISIBLE
                binding.tvAllowServiceEmpty.visibility = View.GONE
            } else {
                binding.recyclerviewAllowService.visibility = View.GONE
                binding.tvAllowServiceEmpty.visibility = View.VISIBLE
            }
            (binding.recyclerviewAllowService.adapter as DetoxServiceMainAdapter).updateItems(it)
        }

        detoxTimeLockViewModel.timeLockItems.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.recyclerviewTimeLock.visibility = View.VISIBLE
                binding.tvTimeLockListEmpty.visibility = View.GONE
            } else {
                binding.recyclerviewTimeLock.visibility = View.GONE
                binding.tvTimeLockListEmpty.visibility = View.VISIBLE
            }
            (binding.recyclerviewTimeLock.adapter as DetoxTimeLockAdapter).submitList(it.toList())
        }
    }

    private fun convertLongFormat(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val remainSeconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainSeconds)
    }

    override fun onStart() {
        super.onStart()
        Log.d("DetoxFragment", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("DetoxFragment", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("DetoxFragment", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("DetoxFragment", "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DetoxFragment", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DetoxFragment", "onDestroy")
    }
}