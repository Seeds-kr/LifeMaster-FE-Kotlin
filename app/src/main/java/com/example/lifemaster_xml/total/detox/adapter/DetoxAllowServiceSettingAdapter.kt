package com.example.lifemaster_xml.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster_xml.databinding.ItemDetoxRepeatLockTargetAppSettingBinding
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxAllowServiceSettingAdapter(
    private val items: ArrayList<DetoxTargetApp>
) : RecyclerView.Adapter<DetoxAllowServiceSettingAdapter.DetoxAllowServiceViewHolder>() {
    inner class DetoxAllowServiceViewHolder(private val binding: ItemDetoxRepeatLockTargetAppSettingBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageResource(item.appIcon)
            binding.ivAppLogo.alpha = if (item.isClicked) 1.0f else 0.4f
        }

        init {
            binding.ivAppLogo.setOnClickListener {
                val position = adapterPosition
                val item = items[position]
                val updateItem = item.copy(isClicked = !item.isClicked)
                items[position] = updateItem // 로그로 찍어봤더니 실제 뷰모델의 값을 건든다. (정확한 근거는 잘 모르겠다. 동일한 객체를 참조하는건지 아니면 복사본을 참조하는건지 잘 모르겠다.)
                notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetoxAllowServiceSettingAdapter.DetoxAllowServiceViewHolder {
        return DetoxAllowServiceViewHolder(
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

    override fun onBindViewHolder(
        holder: DetoxAllowServiceSettingAdapter.DetoxAllowServiceViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }
}