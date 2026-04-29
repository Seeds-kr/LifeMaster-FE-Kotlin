package com.example.lifemaster.presentation.group.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter

object ChartStyle {

    private val PURPLE = Color.parseColor("#AC87CC")
    private val PURPLE_DARK = Color.parseColor("#8F74A8")
    private val PURPLE_LIGHT = Color.parseColor("#D8C7EA")
    private val PURPLE_PALE = Color.parseColor("#4DAC87CC")
    private val GRID = Color.parseColor("#E9E4ED")
    private val AXIS_TEXT = Color.parseColor("#B8B8B8")
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
            isEnabled = true
            axisMinimum = yMin
            axisMaximum = yMax

            setDrawLabels(false)

            setDrawAxisLine(false)
            setDrawZeroLine(false)
            setDrawGridLines(true)
            gridColor = GRID
            gridLineWidth = 1f
            setLabelCount(3, true)

            removeAllLimitLines()

            addLimitLine(yAxisLabelLine(yMax, yMax.toInt().toString()))
            addLimitLine(yAxisLabelLine((yMin + yMax) / 2f, ((yMin + yMax) / 2f).toInt().toString()))
            addLimitLine(yAxisLabelLine(yMin, yMin.toInt().toString()))

            addLimitLine(goalLine(goalY))
        }

        chart.xAxis.apply {
            isEnabled = true
            position = XAxis.XAxisPosition.BOTTOM

            setDrawLabels(true)
            textColor = AXIS_TEXT
            textSize = 11f
            yOffset = 4f

            setDrawAxisLine(false)
            setDrawGridLines(true)

            gridColor = GRID
            gridLineWidth = 1f

            granularity = 1f
            isGranularityEnabled = true

            axisMinimum = -0.5f
            axisMaximum = 5.5f

            setLabelCount(6, true)
            setCenterAxisLabels(false)
            setAvoidFirstLastClipping(false)

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
            isEnabled = true
            axisMinimum = yMin
            axisMaximum = yMax

            setDrawLabels(false)
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
            isEnabled = true
            position = XAxis.XAxisPosition.BOTTOM

            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(true)

            gridColor = GRID
            gridLineWidth = 1f

            granularity = 1f
            isGranularityEnabled = true

            axisMinimum = -0.5f
            axisMaximum = 5.5f

            setLabelCount(6, true)
            setCenterAxisLabels(false)
            setAvoidFirstLastClipping(false)

            valueFormatter = labelFormatter(xLabels)
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
        chart.isDragEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)

        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.setNoDataText("")

        chart.minOffset = 0f
        chart.setExtraOffsets(0f, 0f, 0f, 0f)
        chart.setViewPortOffsets(8f, 20f, 8f, 18f)
    }
    private fun baseCombined(chart: CombinedChart) {
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)

        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.setNoDataText("")

        chart.minOffset = 0f
        chart.minOffset = 0f
        chart.setExtraOffsets(0f, 0f, 0f, 0f)

        chart.setViewPortOffsets(0f, 12f, 0f, 0f)

        chart.setDrawBarShadow(false)
        chart.isHighlightFullBarEnabled = false
        chart.isHighlightPerDragEnabled = false
    }

    private fun yAxisLabelLine(y: Float, label: String): LimitLine {
        return LimitLine(y, label).apply {
            lineColor = Color.TRANSPARENT
            lineWidth = 0.1f

            textColor = AXIS_TEXT
            textSize = 12f

            labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP

            xOffset = 2f
            yOffset = 5f
        }
    }

    private fun goalLine(y: Float): LimitLine {
        return LimitLine(y, "최소목표").apply {
            lineColor = PURPLE
            lineWidth = 1.2f
            enableDashedLine(14f, 10f, 0f)

            textColor = PURPLE
            textSize = 12f
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
    fun circleHoleColor(): Int = WHITE

    fun makeMarkerBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setColor(PURPLE_PALE)
        }
    }

    fun makePomodoroFillDrawable(context: Context): ShapeDrawable {
        return object : ShapeDrawable(RectShape()) {
            override fun draw(canvas: Canvas) {
                val shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    bounds.height().toFloat(),
                    intArrayOf(
                        Color.parseColor("#66AC87CC"),
                        Color.parseColor("#22AC87CC"),
                        Color.parseColor("#00AC87CC")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = shader
                super.draw(canvas)
            }
        }
    }
}