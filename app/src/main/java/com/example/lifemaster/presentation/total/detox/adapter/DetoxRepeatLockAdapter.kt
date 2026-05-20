package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxRepeatLockItemBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import java.util.concurrent.TimeUnit

class DetoxRepeatLockAdapter: ListAdapter<DetoxRepeatLockItem, DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder>(
    differ) {

    inner class DetoxRepeatLockViewHolder(
        private val binding: ItemDetoxRepeatLockItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetoxRepeatLockItem) {
            binding.ivAppLogo.setImageDrawable(item.appIcon)
            binding.tvAppName.text = item.appName

            binding.tvUseTime.text = "${item.useTime}분 사용 시"
            binding.tvLockTime.text = "${item.lockTime}분 잠금"
            binding.tvAccumulateTime.text = formatMinutesToText(
                TimeUnit.MILLISECONDS.toMinutes(item.accumulatedTime).toInt()
            )

            if(item.isMaxTimeLimitSet) {
                binding.tvMaxUseTime.text = "최대 ${item.maxTime}분 사용 가능"
            } else {
                binding.tvMaxUseTime.text = "최대 사용 시간 제한 없음"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetoxRepeatLockViewHolder {
        return DetoxRepeatLockViewHolder(
            ItemDetoxRepeatLockItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DetoxRepeatLockViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun formatMinutesToText(totalMinutes: Int): String {
        val hour = totalMinutes / 60
        val minute = totalMinutes % 60

        return when {
            hour > 0 && minute > 0 -> "${hour}시간 ${minute}분"
            hour > 0 -> "${hour}시간"
            else -> "${minute}분"
        }
    }

    companion object {
        val differ = object: DiffUtil.ItemCallback<DetoxRepeatLockItem>() {
            override fun areContentsTheSame(oldItem: DetoxRepeatLockItem, newItem: DetoxRepeatLockItem): Boolean {
                return oldItem == newItem
            }
            override fun areItemsTheSame(oldItem: DetoxRepeatLockItem, newItem: DetoxRepeatLockItem): Boolean {
                return oldItem === newItem
            }
        }
    }
}