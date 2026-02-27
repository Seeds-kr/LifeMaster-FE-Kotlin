package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxTimeLockBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockResponse

class DetoxTimeLockAdapter(
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<DetoxTimeLockResponse, DetoxTimeLockAdapter.DetoxTimeLockViewHolder>(
    differ
) {

    var allAppList: List<DetoxTargetApp> = emptyList()

    inner class DetoxTimeLockViewHolder(private val binding: ItemDetoxTimeLockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTimeLockResponse) {
            binding.apply {
                val targetApp = allAppList.find { it.appPackageName == item.lockedAppPackageName }
                ivAppLogo.setImageDrawable(targetApp?.appIcon)
                tvAppName.text = targetApp?.appName
                tvWeekType.text = item.cycle.label
                tvDay.text = item.day.label
                tvStartTime.text = String.format("%02d:%02d", item.startHour, item.startMinutes)
                tvStartTimeType.text = item.startAmPm
                tvEndTime.text = String.format("%02d:%02d", item.endHour, item.endMinutes)
                tvEndTimeType.text = item.endAmPm
                ivDeleteTimeLockItem.setOnClickListener {
                    onDeleteClick(item.id)
                }
            }
        }
    }


    companion object {
        val differ = object : DiffUtil.ItemCallback<DetoxTimeLockResponse>() {
            override fun areItemsTheSame(p0: DetoxTimeLockResponse, p1: DetoxTimeLockResponse): Boolean {
                return p0.id == p1.id
            }

            override fun areContentsTheSame(p0: DetoxTimeLockResponse, p1: DetoxTimeLockResponse): Boolean {
                return p0 == p1
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetoxTimeLockViewHolder {
        return DetoxTimeLockViewHolder(
            ItemDetoxTimeLockBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DetoxTimeLockViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}