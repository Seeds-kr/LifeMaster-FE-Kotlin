package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.widget.TextView
import com.example.lifemaster.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class SleepReportMarkerView(context: Context, layoutResource: Int): MarkerView(context, layoutResource) {

    private val sleepScore = findViewById<TextView>(R.id.tv_sleep_score) // TODO: 바인딩으로 바꿔보기

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        sleepScore.text = "수면 점수: ${e?.y.toString()}점"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        return MPPointF(-width.toFloat(), -(height/2).toFloat())
    }
}