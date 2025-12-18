package com.example.lifemaster.presentation.home.alarm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemAlarmBinding
import com.example.lifemaster.presentation.home.alarm.ItemClickListener
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType

class AlarmAdapter(private val itemClickListener: ItemClickListener) : ListAdapter<AlarmModel, AlarmAdapter.ViewHolder>(differ) {
    inner class ViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AlarmModel) = with(binding) {
            tvTime.text = item.timeText
            tvDayOrNight.text = item.ampm
            tvAlarmName.text = item.alarmTitle
            tvMonday.isSelected = item.alarmMon
            tvTuesday.isSelected = item.alarmTue
            tvWednesday.isSelected = item.alarmWed
            tvThursday.isSelected = item.alarmThu
            tvFriday.isSelected = item.alarmFri
            tvSaturday.isSelected = item.alarmSat
            tvSunday.isSelected = item.alarmSun
            when(item.randomMissionType) {
                RandomMissionType.MATH_PROBLEM -> ivRandomMissionMath.isSelected = true
                RandomMissionType.FOLLOW_CLICK -> ivRandomMissionTouch.isSelected = true
                RandomMissionType.TYPING_SENTENCE -> ivRandomMissionWrite.isSelected = true
                null -> Unit
            }
            includeSwitch.alarmSwitch.isChecked = item.switchOnOff
            includeSwitch.alarmSwitch.setOnCheckedChangeListener { view, isChecked ->
                itemClickListener.onSwitchToggle(alarmId = item.id, alarmStatus = isChecked)
            }
            root.setOnClickListener {
                itemClickListener.onItemClick(item.id)
            }
            root.setOnLongClickListener {
                itemClickListener.onItemLongClick(item.id)
                true
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
        val differ = object : DiffUtil.ItemCallback<AlarmModel>() {
            override fun areContentsTheSame(oldItem: AlarmModel, newItem: AlarmModel): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: AlarmModel, newItem: AlarmModel): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}