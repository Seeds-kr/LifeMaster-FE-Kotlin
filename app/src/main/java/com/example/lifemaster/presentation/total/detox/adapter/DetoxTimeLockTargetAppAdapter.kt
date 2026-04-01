package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxTargetAppSettingBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp

class DetoxTimeLockTargetAppAdapter(
    private val onClicked: (DetoxTargetApp) -> Unit
): ListAdapter<DetoxTargetApp, DetoxTimeLockTargetAppAdapter.DetoxTimeLockTargetAppViewHolder>(diffUtil) {

    private var currentPosition = -1

    fun setCurrentPosition(position: Int) {
        currentPosition = position
        notifyItemChanged(currentPosition)
    }

    inner class DetoxTimeLockTargetAppViewHolder(private val binding: ItemDetoxTargetAppSettingBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTargetApp) = with(binding) {
            ivAppLogo.setImageDrawable(item.appIcon)
            ivAppLogo.alpha = if(bindingAdapterPosition == currentPosition) 1.0f else 0.5f

            root.setOnClickListener {

                if(currentPosition == bindingAdapterPosition) return@setOnClickListener

                if(currentPosition != bindingAdapterPosition && currentPosition != RecyclerView.NO_POSITION) {
                    // 기존에 선택한 항목에서 다른 항목이 선택되었을 때
                    val previousPosition = currentPosition
                    currentPosition = bindingAdapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(currentPosition)
                    onClicked(item)
                    return@setOnClickListener
                }

                // 맨 처음 클릭 했을 때
                currentPosition = bindingAdapterPosition
                notifyItemChanged(currentPosition)
                onClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetoxTimeLockTargetAppViewHolder {
        return DetoxTimeLockTargetAppViewHolder(ItemDetoxTargetAppSettingBinding.inflate(
            LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(
        holder: DetoxTimeLockTargetAppViewHolder,
        position: Int
    ) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<DetoxTargetApp>() {
            override fun areItemsTheSame(
                oldItem: DetoxTargetApp,
                newItem: DetoxTargetApp
            ): Boolean {
                return oldItem === newItem
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