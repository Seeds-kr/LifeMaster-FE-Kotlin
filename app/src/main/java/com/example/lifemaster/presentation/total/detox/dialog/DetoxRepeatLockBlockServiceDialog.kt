package com.example.lifemaster.presentation.total.detox.dialog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxRepeatLockBlockServiceBinding
import com.example.lifemaster.presentation.total.detox.adapter.DetoxServiceSettingAdapter
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel

class DetoxRepeatLockBlockServiceDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_block_service) {
    private lateinit var binding: DialogDetoxRepeatLockBlockServiceBinding
    private val repeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockBlockServiceBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.recyclerview.layoutManager = GridLayoutManager(context, 5)
        binding.recyclerview.adapter = DetoxServiceSettingAdapter(repeatLockViewModel.blockServiceApplications)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnApply.setOnClickListener {
            // 선택된 차단할 서비스
            val blockServices = repeatLockViewModel.blockServiceApplications.filter { app -> app.isClicked }

            // viewmodel 변수 업데이트 -> DetoxFragment 에서 UI 업데이트
            repeatLockViewModel.updateBlockServices(blockServices)

            // 서비스로 데이터를 전달하기 위한 브로드 캐스트 송신
            val blockServicePackageNames = arrayListOf<String>()
            blockServices.forEach {
                blockServicePackageNames.add(it.appPackageName)
            }
            val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER")
            intent.putStringArrayListExtra("PERMANENT_BLOCK_SERVICE_APPLICATIONS", blockServicePackageNames)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

            // UI 변경
            Toast.makeText(requireContext(), "서비스의 차단/허용 상태가 변경되었습니다!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockBlockServiceDialog"
    }
}