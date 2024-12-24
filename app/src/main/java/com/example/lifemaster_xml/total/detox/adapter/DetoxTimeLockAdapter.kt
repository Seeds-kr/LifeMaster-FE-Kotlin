package com.example.lifemaster_xml.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.databinding.ItemDetoxTimeLockBinding
import com.example.lifemaster_xml.total.detox.model.DetoxTimeLockItem

class DetoxTimeLockAdapter : ListAdapter<DetoxTimeLockItem, DetoxTimeLockAdapter.DetoxTimeLockViewHolder>(differ) {

    inner class DetoxTimeLockViewHolder(private val binding: ItemDetoxTimeLockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTimeLockItem) {
            binding.tvWeek.text = item.week
            binding.tvDay.text = item.day
            binding.tvTime.text = item.time
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