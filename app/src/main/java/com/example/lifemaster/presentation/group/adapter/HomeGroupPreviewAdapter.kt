package com.example.lifemaster.presentation.home.group.adapter

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

class HomeGroupPreviewAdapter(
    private val onClick: (GroupResponse) -> Unit
) : ListAdapter<GroupResponse, HomeGroupPreviewAdapter.VH>(diff) {

    init {
        setHasStableIds(true)
    }

    private var selectedPos = RecyclerView.NO_POSITION

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_preview, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCurrentListChanged(
        previousList: MutableList<GroupResponse>,
        currentList: MutableList<GroupResponse>
    ) {
        super.onCurrentListChanged(previousList, currentList)

        val size = currentList.size
        if (size == 0) {
            selectedPos = RecyclerView.NO_POSITION
            return
        }
        if (selectedPos != RecyclerView.NO_POSITION && selectedPos >= size) {
            selectedPos = RecyclerView.NO_POSITION
        }
    }

    fun setSelectedByIndex(index: Int) {
        if (itemCount <= 0) return
        val safe = index.coerceIn(0, itemCount - 1)
        if (safe == selectedPos) return

        val prev = selectedPos
        selectedPos = safe

        if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
        notifyItemChanged(selectedPos)
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_group_title)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_group_icon)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val prev = selectedPos
                selectedPos = pos

                if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
                notifyItemChanged(selectedPos)

                onClick(getItem(pos))
            }
        }

        fun bind(item: GroupResponse) {
            tvTitle.text = item.name

            val pos = when {
                bindingAdapterPosition != RecyclerView.NO_POSITION -> bindingAdapterPosition
                absoluteAdapterPosition != RecyclerView.NO_POSITION -> absoluteAdapterPosition
                else -> 0
            }

            val bgRes = if (pos % 2 == 0) {
                R.drawable.bg_group_card
            } else {
                R.drawable.bg_group_card_outline
            }
            itemView.setBackgroundResource(bgRes)
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