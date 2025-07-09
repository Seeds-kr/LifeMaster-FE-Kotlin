package com.example.lifemaster.presentation.home.alarm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemAlarmBinding
import com.example.lifemaster.presentation.home.alarm.model.AlarmItem
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType

class AlarmAdapter : ListAdapter<AlarmItem, AlarmAdapter.ViewHolder>(differ) {
    inner class ViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dayMapping by lazy {
            mapOf(
                "월" to binding.tvMonday,
                "화" to binding.tvTuesday,
                "수" to binding.tvWednesday,
                "목" to binding.tvThursday,
                "금" to binding.tvFriday,
                "토" to binding.tvSaturday,
                "일" to binding.tvSunday
            )
        }

        fun bind(item: AlarmItem) = with(binding) {
            includeSwitch.widget.isChecked = true
            val hour = if (item.time.second in 13..23) item.time.second - 12 else item.time.second
            val minutes = String.format("%02d", item.time.third)
            tvTime.text = "${hour}:${minutes}"
            tvDayOrNight.text = "${item.time.first}"
            tvAlarmName.text = "${item.title}"
            tvAlarmDelay.text = if (item.isDelaySet) {
                "미루기 ${item.delayMinute}분 총 ${item.delayCount}회"
            } else {
                "미루기 없음"
            }
            item.alarmRepeatDays.forEach { day ->
                dayMapping[day]?.isSelected = true
            }
            item.randomMissions.forEach { randomMissionType ->
                when (randomMissionType) {
                    RandomMissionType.MATHEMATICAL_PROBLEM_SOLVING -> ivRandomMissionMath.isSelected =
                        true

                    RandomMissionType.TOUCH_ALONG -> ivRandomMissionTouch.isSelected = true
                    RandomMissionType.WRITE_ALONG -> ivRandomMissionWrite.isSelected = true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<AlarmItem>() {
            override fun areContentsTheSame(oldItem: AlarmItem, newItem: AlarmItem): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: AlarmItem, newItem: AlarmItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}