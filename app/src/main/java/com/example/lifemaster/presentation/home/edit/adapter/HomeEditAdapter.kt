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
import com.example.lifemaster.presentation.home.HomeConfig
import java.util.Collections

class HomeEditAdapter(
    private val itemList: MutableList<String>,
    private val isServiceList: Boolean,
    private val onToggleClick: (String, Boolean) -> Unit,
    private val startDragListener: HomeEditOnStartDragListener? = null,
    private val showToggle: Boolean = true,
    private val labelForItem: ((String) -> String)? = null,
    private val iconTintForItem: ((String) -> String)? = null
) : RecyclerView.Adapter<HomeEditAdapter.ServiceViewHolder>(),
    HomeEditTouchHelperAdapter {

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

        holder.tvServiceName.text = labelForItem?.invoke(item) ?: item
        val tintHex = iconTintForItem?.invoke(item)
            ?: (HomeConfig.HOME_EDIT_ICON_TINT_BY_NAME[item] ?: "#333333")
        holder.ivServiceIcon.setColorFilter(tintHex.toColorInt())
        if (showToggle) {
            holder.ivToggle.visibility = View.VISIBLE
            holder.ivToggle.setImageResource(
                if (isServiceList) R.drawable.ic_toggle_delete else R.drawable.ic_toggle_add
            )
            holder.ivToggle.setOnClickListener { onToggleClick(item, isServiceList) }
        } else {
            holder.ivToggle.visibility = View.GONE
            holder.ivToggle.setOnClickListener(null)
        }

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
