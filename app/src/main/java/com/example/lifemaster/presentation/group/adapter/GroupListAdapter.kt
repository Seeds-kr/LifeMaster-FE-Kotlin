package com.example.lifemaster.presentation.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.group.model.GroupResponse

class GroupListAdapter(
    private val onClick: (GroupResponse) -> Unit
) : ListAdapter<GroupResponse, GroupListAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_group_list, parent, false)
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
            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<GroupResponse>() {
            override fun areItemsTheSame(oldItem: GroupResponse, newItem: GroupResponse) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GroupResponse, newItem: GroupResponse) =
                oldItem == newItem
        }
    }
}