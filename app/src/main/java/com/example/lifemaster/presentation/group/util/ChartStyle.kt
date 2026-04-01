package com.example.lifemaster.presentation.group.util

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter

object ChartStyle {

    private val PURPLE = Color.parseColor("#AC87CC")
    private val PURPLE_DARK = Color.parseColor("#8F74A8")
    private val PURPLE_LIGHT = Color.parseColor("#D7C4EA")
    private val PURPLE_FILL = Color.parseColor("#33AC87CC")

    private val GRID = Color.parseColor("#E3E3E3")
    private val AXIS_TEXT = Color.parseColor("#B7B7B7")
    private val WHITE = Color.WHITE

    fun applySleep(
        chart: LineChart,
        xLabels: List<String>,
        goalY: Float,
        yMin: Float,
        yMax: Float
    ) {
        baseLine(chart)

        chart.axisRight.isEnabled = false

        chart.axisLeft.apply {
            axisMinimum = yMin
            axisMaximum = yMax
            textColor = AXIS_TEXT
            textSize = 10f
            setDrawAxisLine(false)
            setDrawZeroLine(false)
            setDrawGridLines(true)
            gridColor = GRID
            gridLineWidth = 1f
            setLabelCount(3, true)

            removeAllLimitLines()
            addLimitLine(goalLine(goalY))
        }

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = AXIS_TEXT
            textSize = 10f
            setDrawAxisLine(false)
            setDrawGridLines(true)
            gridColor = GRID
            gridLineWidth = 1f
            granularity = 1f
            axisMinimum = 0f
            axisMaximum = (xLabels.size - 1).toFloat()
            setLabelCount(xLabels.size, true)
            valueFormatter = labelFormatter(xLabels)
        }

        chart.legend.isEnabled = false
    }

    fun applyPomodoro(
        chart: CombinedChart,
        xLabels: List<String>,
        goalY: Float,
        yMin: Float,
        yMax: Float
    ) {
        baseCombined(chart)

        chart.axisRight.isEnabled = false

        chart.axisLeft.apply {
            axisMinimum = yMin
            axisMaximum = yMax
            textColor = AXIS_TEXT
            textSize = 10f
            setDrawAxisLine(false)
            setDrawZeroLine(false)
            setDrawGridLines(true)
            gridColor = GRID
            gridLineWidth = 1f
            setLabelCount(3, true)

            removeAllLimitLines()
            addLimitLine(goalLine(goalY))
        }

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = AXIS_TEXT
            textSize = 10f
            setDrawAxisLine(false)
            setDrawGridLines(true)
            gridColor = GRID
            gridLineWidth = 1f
            granularity = 1f
            axisMinimum = -0.5f
            axisMaximum = xLabels.size - 0.5f
            setLabelCount(xLabels.size, true)
            valueFormatter = labelFormatter(xLabels)
            labelRotationAngle = if (xLabels.size >= 6) -20f else 0f
        }

        chart.legend.isEnabled = false
        chart.setDrawOrder(
            arrayOf(
                CombinedChart.DrawOrder.BAR,
                CombinedChart.DrawOrder.LINE
            )
        )
    }

    private fun baseLine(chart: LineChart) {
        chart.setTouchEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.setNoDataText("")
        chart.setExtraOffsets(6f, 8f, 6f, 4f)
        chart.setViewPortOffsets(42f, 16f, 20f, 28f)
    }

    private fun baseCombined(chart: CombinedChart) {
        chart.setTouchEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.setNoDataText("")
        chart.setExtraOffsets(6f, 8f, 6f, 4f)
        chart.setViewPortOffsets(42f, 16f, 20f, 28f)
    }

    private fun goalLine(y: Float): LimitLine {
        return LimitLine(y, "최소목표").apply {
            lineColor = PURPLE
            lineWidth = 1f
            enableDashedLine(12f, 8f, 0f)
            textColor = PURPLE
            textSize = 10f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        }
    }

    private fun labelFormatter(labels: List<String>): ValueFormatter {
        return object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return labels.getOrNull(index).orEmpty()
            }
        }
    }

    fun goalColor(): Int = PURPLE
    fun lineColor(): Int = PURPLE_DARK
    fun barColor(): Int = PURPLE_LIGHT
    fun fillColor(): Int = PURPLE_FILL
    fun circleHoleColor(): Int = WHITE

    fun makeMarkerBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 32f
            setColor(Color.parseColor("#E9DCF2"))
        }
    }
}