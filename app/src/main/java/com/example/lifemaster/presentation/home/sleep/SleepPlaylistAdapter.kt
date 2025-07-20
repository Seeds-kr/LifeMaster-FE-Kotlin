package com.example.lifemaster.presentation.home.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemSleepPlaylistBinding

class SleepPlaylistAdapter: ListAdapter<SleepItem, SleepPlaylistAdapter.SleepPlaylistViewHolder>(differ) {

    inner class SleepPlaylistViewHolder(private val binding: ItemSleepPlaylistBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SleepItem) = with(binding) {
            ivSleepPlaylistItemThumbnail.setImageResource(item.thumbnail)
            tvSleepPlaylistItemTitle.text = item.title
            tvSleepPlaylistItemDuration.text = item.duration
            tvSleepPlaylistItemDescription.text = item.description
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SleepPlaylistViewHolder {
        return SleepPlaylistViewHolder(
            ItemSleepPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: SleepPlaylistViewHolder,
        position: Int
    ) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<SleepItem>() {
            override fun areItemsTheSame(
                oldItem: SleepItem,
                newItem: SleepItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SleepItem,
                newItem: SleepItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}