package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxTargetAppSettingBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp

class DetoxRepeatLockTargetAppAdapter(
    private val onClicked: (DetoxTargetApp) -> Unit
): ListAdapter<DetoxTargetApp, DetoxRepeatLockTargetAppAdapter.DetoxRepeatLockTargetAppViewHolder>(diffUtil) {

    private var currentPosition = RecyclerView.NO_POSITION

    inner class DetoxRepeatLockTargetAppViewHolder(private val binding: ItemDetoxTargetAppSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetoxTargetApp) = with(binding) {
            binding.ivAppLogo.setImageDrawable(item.appIcon)
            ivAppLogo.alpha = if(bindingAdapterPosition == currentPosition) 1.0f else 0.5f

            binding.ivAppLogo.setOnClickListener {
                if(bindingAdapterPosition != currentPosition && currentPosition != RecyclerView.NO_POSITION) {
                    // 기존에 데이터가 있는 상태에서 다른 데이터를 클릭했을 때
                    val previousPosition = currentPosition
                    currentPosition = bindingAdapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(currentPosition)
                    onClicked(item)
                    return@setOnClickListener
                }
                currentPosition = bindingAdapterPosition
                notifyItemChanged(currentPosition)
                onClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetoxRepeatLockTargetAppViewHolder {
        return DetoxRepeatLockTargetAppViewHolder(
            ItemDetoxTargetAppSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DetoxRepeatLockTargetAppViewHolder, position: Int) {
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