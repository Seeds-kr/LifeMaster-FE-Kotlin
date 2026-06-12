package com.example.lifemaster.presentation.home.pomodoro.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroRecentFocusResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.max

class PomodoroRecentFocusGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var items: List<PomodoroRecentFocusResponse> = emptyList()

    private val pomodoroColor = Color.parseColor("#5C76C3")

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pomodoroColor
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E1E5EC")
        strokeWidth = dp(1f)
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B8B8B8")
        textSize = sp(12f)
        textAlign = Paint.Align.CENTER
    }

    private val yAxisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B8B8B8")
        textSize = sp(12f)
        textAlign = Paint.Align.RIGHT
    }

    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(15f)
        textAlign = Paint.Align.CENTER
    }

    private val emptyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8F8F8F")
        textSize = sp(16f)
        textAlign = Paint.Align.CENTER
    }

    private val lastDotOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val lastDotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val lastDotInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BFC3CC")
        style = Paint.Style.FILL
    }

    private val tooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EEF2FF")
        style = Paint.Style.FILL
    }

    private val tooltipTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pomodoroColor
        textSize = sp(12f)
        textAlign = Paint.Align.RIGHT
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val tooltipLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        textSize = sp(10f)
        textAlign = Paint.Align.LEFT
    }

    private val tooltipValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pomodoroColor
        textSize = sp(10f)
        textAlign = Paint.Align.RIGHT
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    fun setItems(newItems: List<PomodoroRecentFocusResponse>) {
        items = newItems
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val graphItems = makeSixDaysItems()
        if (graphItems.isEmpty()) return

        val maxMinute = graphItems.maxOfOrNull { it.totalFocusMinutes } ?: 0
        val maxValue = getNiceMaxValue(maxMinute)
        val yLabels = listOf(0, maxValue / 2, maxValue)

        val left = 80f
        val right = width - 72f
        val top = 60f
        val bottom = height - 134f

        val graphWidth = right - left
        val graphHeight = bottom - top

        yLabels.forEach { value ->
            val rawY = bottom - (value.toFloat() / maxValue.toFloat() * graphHeight)
            val y = rawY.toInt().toFloat() + 0.5f

            canvas.drawLine(left, y, right, y, gridPaint)
            canvas.drawText(value.toString(), left - 20f, y + 5f, yAxisTextPaint)
        }

        val path = Path()

        graphItems.forEachIndexed { index, item ->
            val x = getX(index, graphItems.size, left, graphWidth)
            val y = bottom - (item.totalFocusMinutes.toFloat() / maxValue.toFloat() * graphHeight)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

            canvas.drawText(item.dayText, x, bottom + 42f, textPaint)

            if (item.focusEmoji == "-") {
                canvas.drawText("-", x, bottom + 96f, emptyTextPaint)
            } else {
                canvas.drawText(item.focusEmoji, x, bottom + 96f, emojiPaint)
            }
        }

        canvas.drawPath(path, linePaint)

        val lastIndex = graphItems.lastIndex
        val lastItem = graphItems[lastIndex]
        val lastX = getX(lastIndex, graphItems.size, left, graphWidth)
        val lastY = bottom - (lastItem.totalFocusMinutes.toFloat() / maxValue.toFloat() * graphHeight)

        drawTooltip(canvas, lastItem, lastX, lastY)

        canvas.drawCircle(lastX, lastY, 12f, lastDotOuterPaint)
        canvas.drawCircle(lastX, lastY, 8f, lastDotStrokePaint)
        canvas.drawCircle(lastX, lastY, 5f, lastDotInnerPaint)
    }

    private fun drawTooltip(
        canvas: Canvas,
        item: GraphItem,
        lastX: Float,
        lastY: Float
    ) {
        val cardWidth = dp(140f)
        val cardHeight = dp(72f)

        var cardRight = lastX - dp(12f)
        var cardLeft = cardRight - cardWidth

        if (cardLeft < dp(16f)) {
            cardLeft = dp(16f)
            cardRight = cardLeft + cardWidth
        }

        val cardTop = max(dp(24f), lastY - dp(58f))
        val cardBottom = cardTop + cardHeight

        val rect = RectF(cardLeft, cardTop, cardRight, cardBottom)
        canvas.drawRoundRect(rect, dp(6f), dp(6f), tooltipPaint)

        canvas.drawText(
            "${item.totalFocusMinutes}분 집중",
            cardRight - dp(12f),
            cardTop + dp(20f),
            tooltipTitlePaint
        )

        canvas.drawText(
            "완료한 뽀모도로",
            cardLeft + dp(12f),
            cardTop + dp(43f),
            tooltipLabelPaint
        )

        canvas.drawText(
            "${item.completedCount}회",
            cardRight - dp(12f),
            cardTop + dp(43f),
            tooltipValuePaint
        )

        canvas.drawText(
            "평균 집중 시간",
            cardLeft + dp(12f),
            cardTop + dp(61f),
            tooltipLabelPaint
        )

        canvas.drawText(
            "${item.averageFocusMinutes}분",
            cardRight - dp(12f),
            cardTop + dp(61f),
            tooltipValuePaint
        )
    }

    private fun makeSixDaysItems(): List<GraphItem> {
        val today = LocalDate.now()
        val itemMap = items.associateBy { it.date }

        return (5 downTo 0).map { diff ->
            val date = today.minusDays(diff.toLong())
            val dateText = date.format(DateTimeFormatter.ISO_DATE)
            val serverItem = itemMap[dateText]

            GraphItem(
                date = date,
                dayText = date.dayOfMonth.toString(),
                totalFocusMinutes = serverItem?.totalFocusMinutes ?: 0,
                completedCount = serverItem?.completedCount ?: 0,
                averageFocusMinutes = serverItem?.averageFocusMinutes ?: 0,
                focusEmoji = when (serverItem?.focusLevel?.trim()?.uppercase()) {
                    "LOW" -> "😵"
                    "NORMAL" -> "😐"
                    "GOOD" -> "🙂"
                    "VERY_GOOD" -> "🔥"
                    else -> "-"
                }
            )
        }
    }

    private fun getX(index: Int, size: Int, left: Float, graphWidth: Float): Float {
        return if (size == 1) {
            left + graphWidth / 2f
        } else {
            left + graphWidth / (size - 1) * index
        }
    }

    private fun getNiceMaxValue(maxMinute: Int): Int {
        if (maxMinute <= 0) return 60

        return when {
            maxMinute <= 60 -> 60
            maxMinute <= 120 -> 120
            maxMinute <= 180 -> 180
            else -> ceil(maxMinute / 60f).toInt() * 60
        }.let { max(it, 60) }
    }

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            resources.displayMetrics
        )
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }

    private data class GraphItem(
        val date: LocalDate,
        val dayText: String,
        val totalFocusMinutes: Int,
        val completedCount: Int,
        val averageFocusMinutes: Int,
        val focusEmoji: String
    )
}