package com.example.lifemaster.presentation.group.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.group.model.GroupAchievementHeatmapItem

class RecentAchieveHeatmapAdapter(
    private val items: List<GroupAchievementHeatmapItem>,
    private val onItemSelected: (
        item: GroupAchievementHeatmapItem,
        anchorView: View,
        isNowSelected: Boolean,
        position: Int
    ) -> Unit
) : RecyclerView.Adapter<RecentAchieveHeatmapAdapter.HeatmapViewHolder>() {

    private val maxCount = items.maxOfOrNull { it.achievedUserCount } ?: 0
    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeatmapViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_achieve, parent, false)
        return HeatmapViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeatmapViewHolder, position: Int) {
        holder.bind(
            item = items[position],
            maxCount = maxCount,
            isSelected = position == selectedPosition
        )

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.bindingAdapterPosition
            if (clickedPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val oldPosition = selectedPosition

            if (selectedPosition == clickedPosition) {
                selectedPosition = -1
                notifyItemChanged(clickedPosition)
                onItemSelected(items[clickedPosition], holder.itemView, false, clickedPosition)
            } else {
                selectedPosition = clickedPosition
                if (oldPosition != -1) notifyItemChanged(oldPosition)
                notifyItemChanged(clickedPosition)
                onItemSelected(items[clickedPosition], holder.itemView, true, clickedPosition)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun clearSelection() {
        val oldPosition = selectedPosition
        selectedPosition = -1
        if (oldPosition != -1) notifyItemChanged(oldPosition)
    }

    class HeatmapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cellBg: View = itemView.findViewById(R.id.view_cell_bg)
        private val selectedDot: View = itemView.findViewById(R.id.view_selected_dot)

        fun bind(
            item: GroupAchievementHeatmapItem,
            maxCount: Int,
            isSelected: Boolean
        ) {
            val color = getHeatmapColor(item.achievedUserCount, maxCount)
            val bg = cellBg.background.mutate() as GradientDrawable
            bg.setColor(color)
            bg.setStroke(0, Color.TRANSPARENT)

            selectedDot.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        private fun getHeatmapColor(count: Int, maxCount: Int): Int {
            if (count <= 0 || maxCount <= 0) {
                return Color.parseColor("#F0F4F5")
            }

            val ratio = count.toFloat() / maxCount.toFloat()

            return when {
                ratio <= 0.25f -> Color.parseColor("#E6DDF1")
                ratio <= 0.50f -> Color.parseColor("#D7C4EA")
                ratio <= 0.75f -> Color.parseColor("#BC97DC")
                else -> Color.parseColor("#AC87CC")
            }
        }
    }
}