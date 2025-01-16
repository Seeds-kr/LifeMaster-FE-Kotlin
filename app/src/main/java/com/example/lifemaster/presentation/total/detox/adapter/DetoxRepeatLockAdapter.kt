package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxRepeatLockItemBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import java.util.concurrent.TimeUnit

class DetoxRepeatLockAdapter: RecyclerView.Adapter<DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder>() {

    private var items: ArrayList<DetoxRepeatLockItem> = arrayListOf()

    fun updateItems(newItems: ArrayList<DetoxRepeatLockItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class DetoxRepeatLockViewHolder(private val binding: ItemDetoxRepeatLockItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxRepeatLockItem) {
            binding.ivAppLogo.setImageDrawable(item.appIcon)
            binding.tvAppName.text = item.appName
            binding.tvUseTime.text = "${item.useTime}분"
            binding.tvLockTime.text = "${item.lockTime}분"
            binding.tvAccumulateTime.text = "${TimeUnit.MILLISECONDS.toMinutes(item.accumulatedTime)}분"
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

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetoxRepeatLockViewHolder, position: Int) {
        holder.bind(items[position])
    }
}