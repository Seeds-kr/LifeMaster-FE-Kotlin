package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxTargetAppMainBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp

class DetoxPermanentLockAdapter: ListAdapter<DetoxTargetApp, DetoxPermanentLockAdapter.DetoxPermanentLockViewHolder>(diffUtil) {

    inner class DetoxPermanentLockViewHolder(private val binding: ItemDetoxTargetAppMainBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetoxTargetApp) {
            binding.ivAppLogo.setImageDrawable(item.appIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetoxPermanentLockViewHolder {
        return DetoxPermanentLockViewHolder(
            ItemDetoxTargetAppMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DetoxPermanentLockViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<DetoxTargetApp>() {
            override fun areItemsTheSame(
                oldItem: DetoxTargetApp,
                newItem: DetoxTargetApp
            ): Boolean {
                return oldItem === newItem
            }
            override fun areContentsTheSame(
                oldItem: DetoxTargetApp,
                newItem: DetoxTargetApp
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}