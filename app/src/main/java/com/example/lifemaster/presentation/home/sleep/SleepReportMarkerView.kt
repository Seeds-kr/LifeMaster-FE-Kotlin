package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.widget.TextView
import com.example.lifemaster.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.time.Duration
import java.time.LocalTime

class SleepReportMarkerView(
    context: Context,
    layoutResource: Int,
    private val dailySleepDurations: List<String>,
    private val dailyAlarmDurations: List<String>
) : MarkerView(context, layoutResource) {

    private val totalSleepTime = findViewById<TextView>(R.id.tv_total_sleep_time)
    private val sleepScore = findViewById<TextView>(R.id.tv_sleep_score_value)
    private val timeToWakeUp = findViewById<TextView>(R.id.tv_time_to_wake_up_value)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val position = e?.x?.toInt() ?: return
        totalSleepTime.text = "${dailySleepDurations[position]} 수면"
        sleepScore.text = "0점"
        timeToWakeUp.text = "${getMinuteDifference(dailyAlarmDurations[position])}분"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        return MPPointF(-width.toFloat() - 20f, -(height / 2).toFloat())
    }

    private fun getMinuteDifference(timeRange: String): Int {
        val separatedTime = timeRange.split("~").map { it.trim() }
        val start = LocalTime.parse(separatedTime[0])
        val end = LocalTime.parse(separatedTime[1])
        val difference = Duration.between(start, end).toMinutes().toInt()
        return difference
    }
}