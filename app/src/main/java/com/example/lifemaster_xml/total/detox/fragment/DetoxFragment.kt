package com.example.lifemaster_xml.total.detox.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.FragmentDetoxBinding
import com.example.lifemaster_xml.total.detox.adapter.DetoxServiceMainAdapter
import com.example.lifemaster_xml.total.detox.adapter.DetoxRepeatLockAdapter
import com.example.lifemaster_xml.total.detox.adapter.DetoxTimeLockAdapter
import com.example.lifemaster_xml.total.detox.dialog.DetoxRepeatLockBlockServiceDialog
import com.example.lifemaster_xml.total.detox.dialog.DetoxTimeLockAllowServiceDialog
import com.example.lifemaster_xml.total.detox.dialog.DetoxRepeatLockDialog
import com.example.lifemaster_xml.total.detox.dialog.DetoxTimeLockDialog
import com.example.lifemaster_xml.total.detox.model.DetoxTimeLockItem
import com.example.lifemaster_xml.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster_xml.total.detox.viewmodel.DetoxTimeLockViewModel

class DetoxFragment : Fragment(R.layout.fragment_detox) {

    lateinit var binding: FragmentDetoxBinding
    private val detoxTimeLockViewModel: DetoxTimeLockViewModel by activityViewModels()
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

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
        val dummyData = arrayListOf(
            DetoxTimeLockItem(0, "매주", "월요일", "01:00PM - 04:00PM"),
            DetoxTimeLockItem(1, "매일", "화요일", "04:00PM - 05:00PM"),
            DetoxTimeLockItem(2, "매일", "금요일", "02:00PM - 08:00PM"),
            DetoxTimeLockItem(3, "매주", "수요일", "09:00PM - 10:00PM"),
            DetoxTimeLockItem(4, "격주", "토요일", "03:00PM - 04:00PM"),
            DetoxTimeLockItem(5, "격주", "토요일", "03:00PM - 04:00PM"),
            DetoxTimeLockItem(6, "격주", "토요일", "03:00PM - 04:00PM"),
            DetoxTimeLockItem(7, "격주", "토요일", "03:00PM - 04:00PM"),
            DetoxTimeLockItem(8, "격주", "토요일", "03:00PM - 04:00PM")
        )
        val detoxTimeLockAdapter = DetoxTimeLockAdapter()
        binding.recyclerviewTimeLock.adapter = detoxTimeLockAdapter
        detoxTimeLockAdapter.submitList(dummyData)

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
            val dialog = DetoxRepeatLockDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxRepeatLockDialog.TAG)
        }

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

    private fun observing() {

        detoxRepeatLockViewModel.blockServices.observe(viewLifecycleOwner) {
            (binding.recyclerviewBlockService.adapter as DetoxServiceMainAdapter).updateItems(it)
        }

        detoxRepeatLockViewModel.repeatLockApp.observe(viewLifecycleOwner) {
            (binding.recyclerviewRepeatLock.adapter as DetoxRepeatLockAdapter).updateItems(it)
        }

        detoxTimeLockViewModel.allowServices.observe(viewLifecycleOwner) {
            (binding.recyclerviewAllowService.adapter as DetoxServiceMainAdapter).updateItems(it)
        }
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