package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxTargetAppSettingBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp

// 반복 잠금과 시간 잠금 둘 다 공유 가능한 어댑터
class DetoxPermanentLockServiceSettingAdapter: ListAdapter<DetoxTargetApp, DetoxPermanentLockServiceSettingAdapter.DetoxPermanentLockServiceSettingViewHolder>(diffUtil) {

    private val selectedPackages = mutableSetOf<String>()

    fun getSelectedPackageNames(): List<String> {
        return selectedPackages.toList()
    }

    inner class DetoxPermanentLockServiceSettingViewHolder(private val binding: ItemDetoxTargetAppSettingBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageDrawable(item.appIcon)
            updateAlpha(item.appPackageName)
            binding.ivAppLogo.setOnClickListener {
                val packageName = item.appPackageName
                if(selectedPackages.contains(packageName)) {
                    selectedPackages.remove(packageName)
                } else {
                    selectedPackages.add(packageName)
                }
                updateAlpha(packageName)
            }
        }

        private fun updateAlpha(packageName: String) {
            val alpha = if(selectedPackages.contains(packageName)) 1.0f else 0.3f
            binding.ivAppLogo.alpha = alpha
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetoxPermanentLockServiceSettingViewHolder {
        return DetoxPermanentLockServiceSettingViewHolder(ItemDetoxTargetAppSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: DetoxPermanentLockServiceSettingViewHolder,
        position: Int
    ) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<DetoxTargetApp>() {
            override fun areItemsTheSame(
                oldItem: DetoxTargetApp,
                newItem: DetoxTargetApp
            ): Boolean {
                return oldItem.appPackageName == newItem.appPackageName
            }
            override fun areContentsTheSame(
                oldItem: DetoxTargetApp,
                newItem: DetoxTargetApp
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}