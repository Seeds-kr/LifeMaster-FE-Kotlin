package com.example.lifemaster.presentation.total.detox.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemDetoxRepeatLockItemBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import java.util.concurrent.TimeUnit

class DetoxRepeatLockAdapter(
    private val onDeleteClick: (DetoxRepeatLockItem) -> Unit
) : ListAdapter<DetoxRepeatLockItem, DetoxRepeatLockAdapter.DetoxRepeatLockViewHolder>(differ) {

    private var openedForeground: android.view.View? = null

    inner class DetoxRepeatLockViewHolder(
        val binding: ItemDetoxRepeatLockItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var downX = 0f
        private var currentX = 0f

        fun bind(item: DetoxRepeatLockItem) {
            binding.swipeForeground.translationX = 0f

            binding.ivAppLogo.setImageDrawable(item.appIcon)
            binding.tvAppName.text = item.appName

            binding.tvUseTime.text = "${item.useTime}분"
            binding.tvLockTime.text = "${item.lockTime}분"
            binding.tvAccumulateTime.text =
                "${TimeUnit.MILLISECONDS.toMinutes(item.accumulatedTime)}분"

            binding.tvMaxUseTime.text = if (item.isMaxTimeLimitSet) {
                "최대 ${item.maxTime}분 사용 가능"
            } else {
                "최대 사용 시간 제한 없음"
            }

            binding.btnDeleteRepeatLock.setOnClickListener {
                onDeleteClick(item)
            }

            binding.swipeForeground.setOnTouchListener { view, event ->
                val revealPx = 72f * view.resources.displayMetrics.density

                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        if (openedForeground != null && openedForeground != view) {
                            openedForeground?.animate()
                                ?.translationX(0f)
                                ?.setDuration(150)
                                ?.start()

                            openedForeground = null
                        }

                        downX = event.rawX
                        currentX = view.translationX
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val diff = event.rawX - downX
                        val nextX = (currentX + diff).coerceIn(-revealPx, 0f)

                        view.translationX = nextX
                        true
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {

                        if (view.translationX <= -revealPx / 2) {

                            view.animate()
                                .translationX(-revealPx)
                                .setDuration(150)
                                .start()

                            openedForeground = view

                        } else {

                            view.animate()
                                .translationX(0f)
                                .setDuration(150)
                                .start()

                            if (openedForeground == view) {
                                openedForeground = null
                            }
                        }

                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetoxRepeatLockViewHolder {
        return DetoxRepeatLockViewHolder(
            ItemDetoxRepeatLockItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DetoxRepeatLockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun closeOpenedItem() {
        openedForeground?.animate()
            ?.translationX(0f)
            ?.setDuration(150)
            ?.start()

        openedForeground = null
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<DetoxRepeatLockItem>() {
            override fun areContentsTheSame(
                oldItem: DetoxRepeatLockItem,
                newItem: DetoxRepeatLockItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: DetoxRepeatLockItem,
                newItem: DetoxRepeatLockItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}