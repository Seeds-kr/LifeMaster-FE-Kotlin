package com.example.lifemaster_xml.total.detox.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.databinding.ItemDetoxRepeatLockTargetAppSettingBinding
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxRepeatLockTargetAppAdapter: RecyclerView.Adapter<DetoxRepeatLockTargetAppAdapter.DetoxRepeatLockTargetAppViewHolder>() {

    private var items: ArrayList<DetoxTargetApp> = arrayListOf()
    private var currentPosition = RecyclerView.NO_POSITION

    fun setItems(newItems: ArrayList<DetoxTargetApp>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class DetoxRepeatLockTargetAppViewHolder(private val binding: ItemDetoxRepeatLockTargetAppSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageResource(item.appIcon)
            binding.ivAppLogo.alpha = if (item.isClicked) 1.0f else 0.4f
        }

        init {
            binding.ivAppLogo.setOnClickListener {
                currentPosition = items.indexOfFirst { it.isClicked }
                val previousPosition = currentPosition // 다이얼로그를 나갔다가 다시 들어올 때 정상적으로 작동은 하지만 아쉬운 코드 (나갔다 들어올때만 한번 작동하면 되는건데, 코드상 위치로는 매번 클릭할 때마다 작동해서 낭비가 됨. 근데 다른 방법이 생각이 안남.)
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
            ItemDetoxRepeatLockTargetAppSettingBinding.inflate(
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