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
            // 클릭한 서비스를 변수로 받아오기
            val blockServices = repeatLockViewModel.blockServiceApplications.filter { app -> app.isClicked }

            // viewmodel 변수 업데이트
            repeatLockViewModel.updateBlockServices(blockServices)

            // 브로드 캐스트 송신
            val blockServicePackageNames = arrayListOf<String>()
            blockServices.forEach {
                blockServicePackageNames.add(it.appPackageName)
            }
            val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER")
            intent.putStringArrayListExtra("BLOCK_SERVICE_APPLICATIONS", blockServicePackageNames)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

            // 다이얼로그 해제
            Toast.makeText(requireContext(), "해당 앱은 더이상 사용할 수 없습니다!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockBlockServiceDialog"
    }
}