package com.example.lifemaster.presentation.home.edit.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import java.util.*

class HomeEditAdapter(
    private val itemList: MutableList<String>,
    private val isServiceList: Boolean,
    private val onToggleClick: (String, Boolean) -> Unit,
    private val startDragListener: OnStartDragListener? = null
) : RecyclerView.Adapter<HomeEditAdapter.ServiceViewHolder>(), ItemTouchHelperAdapter {

    private val iconTintMap = mapOf(
        "수면" to "#333333",
        "집중 시간" to "#B4D775",
        "그룹 바로가기" to "#AC87CC",
        "자아성찰 바로가기" to "#FFB943",
        "챌린지" to "#84CAE2",
        "앱 잠금 설정" to "#B4D775",
        "자유 게시판" to "#EA9F95",
        "개선 게시판" to "#EA9F95",
        "알람 추가" to "#BBAB94"
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

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvServiceName.text = item

        val tintColor = Color.parseColor(iconTintMap[item] ?: "#333333")
        holder.ivServiceIcon.setColorFilter(tintColor)

        holder.ivToggle.setImageResource(
            if (isServiceList) R.drawable.ic_toggle_delete else R.drawable.ic_toggle_add
        )
        holder.ivToggle.setOnClickListener {
            onToggleClick(item, isServiceList)
        }

        holder.ivDragHandle.visibility = View.VISIBLE
        holder.ivDragHandle.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startDragListener?.onStartDrag(holder)
            }
            false
        }
    }

    override fun getItemCount(): Int = itemList.size
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(itemList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }
}