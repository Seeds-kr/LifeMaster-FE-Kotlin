package com.example.lifemaster_xml.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.databinding.ItemDetoxRepeatLockTargetAppBinding
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxAllowServiceAdapter(
    private val items: ArrayList<DetoxTargetApp>
): RecyclerView.Adapter<DetoxAllowServiceAdapter.DetoxAllowServiceViewHolder>() {
    inner class DetoxAllowServiceViewHolder(private val binding: ItemDetoxRepeatLockTargetAppBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageResource(item.appIcon)
            binding.ivAppLogo.alpha = if(item.isClicked) 1.0f else 0.4f
        }

        init {
            binding.ivAppLogo.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    val updateItem = item.copy(isClicked = !item.isClicked)
                    items[position] = updateItem
                    notifyItemChanged(position)
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetoxAllowServiceAdapter.DetoxAllowServiceViewHolder {
        return DetoxAllowServiceViewHolder(
            ItemDetoxRepeatLockTargetAppBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetoxAllowServiceAdapter.DetoxAllowServiceViewHolder, position: Int) {
        holder.bind(items[position])
    }
}