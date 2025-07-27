package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.widget.TextView
import com.example.lifemaster.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class SleepReportMarkerView(context: Context, layoutResource: Int): MarkerView(context, layoutResource) {

    private val totalSleepTime = findViewById<TextView>(R.id.tv_total_sleep_time)
    private val sleepScore = findViewById<TextView>(R.id.tv_sleep_score_value)
    private val timeToWakeUp = findViewById<TextView>(R.id.tv_time_to_wake_up_value)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        totalSleepTime.text = "6시간 30분 수면"
        sleepScore.text = "${e?.y?.toInt()}점"
        timeToWakeUp.text = "120분"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        return MPPointF(-width.toFloat(), -(height/2).toFloat())
    }
}