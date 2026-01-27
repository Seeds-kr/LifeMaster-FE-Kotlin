package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.widget.TextView
import com.example.lifemaster.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlin.math.round

class SleepReportMarkerView(
    context: Context,
    layoutResource: Int,
    private val dailySleepDurations: List<String>,
    private val dailySleepScores: List<Float>,
    private val dailyAlarmDurations: List<Int>
) : MarkerView(context, layoutResource) {

    private val totalSleepTime = findViewById<TextView>(R.id.tv_total_sleep_time)
    private val sleepScore = findViewById<TextView>(R.id.tv_sleep_score_value)
    private val timeToWakeUp = findViewById<TextView>(R.id.tv_time_to_wake_up_value)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val position = e?.x?.toInt() ?: return
        totalSleepTime.text = "${dailySleepDurations[position]} 수면"
        sleepScore.text = "${round(dailySleepScores[position]).toInt()}점"
        timeToWakeUp.text = if(dailyAlarmDurations[position] == NO_ALARM_SETTING_DEFAULT_VALUE) "미측정" else "${dailyAlarmDurations[position]}분"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        return MPPointF(-width.toFloat() - 20f, -(height / 2).toFloat())
    }

    companion object {
        private const val NO_ALARM_SETTING_DEFAULT_VALUE = -1000
    }
}