package com.example.lifemaster.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxTargetAppMainBinding
import com.example.lifemaster.total.detox.model.DetoxTargetApp

class DetoxServiceMainAdapter: RecyclerView.Adapter<DetoxServiceMainAdapter.DetoxAllowServiceMainViewHolder>() {

    private var items: List<DetoxTargetApp> = listOf()

    fun updateItems(newItems: List<DetoxTargetApp>) {
        items = newItems
        notifyDataSetChanged() // 나중에 position 받아오는걸로 리팩토링 하기
    }

    // 나중에 ListAdapter 등 다른 어댑터로 마이그레이션 해보기
    inner class DetoxAllowServiceMainViewHolder(private val binding: ItemDetoxTargetAppMainBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageDrawable(item.appIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetoxAllowServiceMainViewHolder {
        return DetoxAllowServiceMainViewHolder(
            ItemDetoxTargetAppMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetoxAllowServiceMainViewHolder, position: Int) {
        holder.bind(items[position])
    }
}