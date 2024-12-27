package com.example.lifemaster_xml.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.databinding.ItemDetoxRepeatLockTargetAppBinding
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxRepeatLockTargetAppAdapter(
    private val items: ArrayList<DetoxTargetApp>
) : RecyclerView.Adapter<DetoxRepeatLockTargetAppAdapter.DetoxRepeatLockTargetAppViewHolder>() {

    private var currentPosition = RecyclerView.NO_POSITION

    inner class DetoxRepeatLockTargetAppViewHolder(private val binding: ItemDetoxRepeatLockTargetAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageResource(item.appIcon)
            binding.ivAppLogo.alpha = if (item.isClicked) 1.0f else 0.4f
        }

        init {
            binding.ivAppLogo.setOnClickListener {
                val previousPosition = currentPosition
                if (previousPosition != RecyclerView.NO_POSITION) {
                    val previousItem = items[previousPosition]
                    currentPosition = adapterPosition
                    val currentItem = items[currentPosition]

                    items[previousPosition] = previousItem.copy(isClicked = false)
                    items[currentPosition] = currentItem.copy(isClicked = true)

                    notifyItemChanged(previousPosition)
                    notifyItemChanged(currentPosition)
                } else {
                    currentPosition = adapterPosition
                    val currentItem = items[currentPosition]
                    items[currentPosition] = currentItem.copy(isClicked = true)
                    notifyItemChanged(currentPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetoxRepeatLockTargetAppViewHolder {
        return DetoxRepeatLockTargetAppViewHolder(
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

    override fun onBindViewHolder(holder: DetoxRepeatLockTargetAppViewHolder, position: Int) {
        holder.bind(items[position])
    }
}