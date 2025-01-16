package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxTimeLockBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockItem

class DetoxTimeLockAdapter : ListAdapter<DetoxTimeLockItem, DetoxTimeLockAdapter.DetoxTimeLockViewHolder>(
    differ
) {

    inner class DetoxTimeLockViewHolder(private val binding: ItemDetoxTimeLockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTimeLockItem) {
            binding.apply {
                tvWeekType.text = item.weekType
                tvDay.text = item.day
                tvStartHour.text = item.startHour
                tvStartMinutes.text = item.startMinutes
                tvStartType.text = item.startType
                tvEndHour.text = item.endHour
                tvEndMinutes.text = item.endMinutes
                tvEndType.text = item.endType
            }
        }
    }


    companion object {
        val differ = object : DiffUtil.ItemCallback<DetoxTimeLockItem>() {
            override fun areItemsTheSame(p0: DetoxTimeLockItem, p1: DetoxTimeLockItem): Boolean {
                return p0.itemId == p1.itemId
            }

            override fun areContentsTheSame(p0: DetoxTimeLockItem, p1: DetoxTimeLockItem): Boolean {
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