package com.example.lifemaster.presentation.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.group.model.GroupResponse

class GroupListAdapter(
    private val onClick: (GroupResponse) -> Unit
) : ListAdapter<GroupResponse, GroupListAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_list, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        itemView: View,
        private val onClick: (GroupResponse) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tv_group_title)
        private val tvSub: TextView = itemView.findViewById(R.id.tv_group_participants_num)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_group_icon)

        fun bind(item: GroupResponse) {
            tvTitle.text = item.name
            tvSub.text = "${item.memberCount ?: 0}명 참여 중"

            ivIcon.setImageResource(getGroupIconRes(item.icon))

            itemView.setOnClickListener {
                onClick(item)
            }
        }

        @DrawableRes
        private fun getGroupIconRes(icon: String?): Int {
            val key = icon.orEmpty()
                .substringBefore("|")
                .ifBlank { "ic_group" }

            return when (key) {
                "ic_alarm" -> R.drawable.ic_alarm
                "ic_book_open" -> R.drawable.ic_book_open
                "ic_calendar" -> R.drawable.ic_calendar
                "ic_certificate" -> R.drawable.ic_certificate
                "ic_chart" -> R.drawable.ic_chart
                "ic_clock" -> R.drawable.ic_clock
                "ic_clock_sleep" -> R.drawable.ic_clock_sleep
                "ic_community" -> R.drawable.ic_community
                "ic_group" -> R.drawable.ic_group
                "ic_home" -> R.drawable.ic_home
                else -> R.drawable.ic_group
            }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<GroupResponse>() {
            override fun areItemsTheSame(
                oldItem: GroupResponse,
                newItem: GroupResponse
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: GroupResponse,
                newItem: GroupResponse
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}