package com.example.lifemaster.presentation.group.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemAlarmBinding
import com.example.lifemaster.presentation.group.model.AlarmItem

class AlarmAdapter : ListAdapter<AlarmItem, AlarmAdapter.ViewHolder>(differ) {
    inner class ViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlarmItem) {
            binding.apply {
                val hour = if(item.time.second in 13..23) item.time.second-12 else item.time.second
                tvTime.text = "${hour}:${item.time.third}"
                tvDayOrNight.text = "${item.time.first}"
                tvAlarmName.text = "${item.title}"
                tvAlarmDelay.text = if (item.isDelaySet) {
                    "미루기 ${item.delayMinute}분 총 ${item.delayCount}회"
                } else {
                    "미루기 없음"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAlarmBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object: DiffUtil.ItemCallback<AlarmItem>() {
            override fun areContentsTheSame(oldItem: AlarmItem, newItem: AlarmItem): Boolean {
                return oldItem == newItem
            }
            override fun areItemsTheSame(oldItem: AlarmItem, newItem: AlarmItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}