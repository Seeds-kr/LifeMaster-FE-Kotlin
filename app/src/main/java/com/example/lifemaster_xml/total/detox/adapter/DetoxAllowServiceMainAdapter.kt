package com.example.lifemaster_xml.total.detox.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.databinding.ItemDetoxRepeatLockTargetAppMainBinding
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxAllowServiceMainAdapter: RecyclerView.Adapter<DetoxAllowServiceMainAdapter.DetoxAllowServiceMainViewHolder>() {

    private var items: List<DetoxTargetApp> = listOf()

    fun setItems(newItems: List<DetoxTargetApp>) {
        items = newItems
        notifyDataSetChanged() // 나중에 position 받아오는걸로 리팩토링 하기
    }

    // 나중에 ListAdapter 등 다른 어댑터로 마이그레이션 해보기
    inner class DetoxAllowServiceMainViewHolder(private val binding: ItemDetoxRepeatLockTargetAppMainBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageResource(item.appIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetoxAllowServiceMainViewHolder {
        return DetoxAllowServiceMainViewHolder(
            ItemDetoxRepeatLockTargetAppMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        Log.d("adapter",""+items.size)
        return items.size
    }

    override fun onBindViewHolder(holder: DetoxAllowServiceMainViewHolder, position: Int) {
        holder.bind(items[position])
    }
}