package com.example.lifemaster.presentation.home.edit.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import java.util.Collections

class HomeEditAdapter(
    private val itemList: MutableList<String>,
    private val isServiceList: Boolean,
    private val onToggleClick: (String, Boolean) -> Unit,
    private val startDragListener: HomeEditOnStartDragListener? = null
) : RecyclerView.Adapter<HomeEditAdapter.ServiceViewHolder>(),
    HomeEditTouchHelperAdapter {

    private val iconTintMap = mapOf(
        "수면" to "#333333",
        "디톡스" to "#B4D775",
        "그룹 바로가기" to "#AC87CC",
        "자아성찰 바로가기" to "#FFB943",
        "챌린지" to "#6DABD9",
        "알람" to "#BBAB94"
    )

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivServiceIcon: ImageView = itemView.findViewById(R.id.ivServiceIcon)
        val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        val ivToggle: ImageView = itemView.findViewById(R.id.ivToggle)
        val ivDragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_edit_service, parent, false)
        return ServiceViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = itemList[position]

        holder.tvServiceName.text = item
        holder.ivServiceIcon.setColorFilter((iconTintMap[item] ?: "#333333").toColorInt())
        holder.ivToggle.setImageResource(
            if (isServiceList) R.drawable.ic_toggle_delete else R.drawable.ic_toggle_add
        )
        holder.ivToggle.setOnClickListener { onToggleClick(item, isServiceList) }

        holder.ivDragHandle.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                v.findParentRecyclerView()?.let { rv ->
                    startDragListener?.onStartDrag(rv, holder)
                }
            }
            false
        }
    }

    override fun getItemCount(): Int = itemList.size

    override fun getItemId(position: Int): Long =
        itemList[position].hashCode().toLong()

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition == toPosition) return false
        Collections.swap(itemList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    private fun View.findParentRecyclerView(): RecyclerView? {
        var p = parent
        while (p is ViewGroup) {
            if (p is RecyclerView) return p
            p = p.parent
        }
        return null
    }
}
