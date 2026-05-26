package com.example.lifemaster.presentation.total.detox.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DetoxCircularTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progressAngle: Float = 360f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width / 2f) - 10f

        val rectF = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // 배경 원
        paint.shader = null
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius, paint)

        // 진행 원
        val sweepGradient = SweepGradient(
            centerX,
            centerY,
            intArrayOf(
                Color.parseColor("#B4D775"),
                Color.parseColor("#EAF4D2")
            ),
            floatArrayOf(0.0f, progressAngle / 360f)
        )

        // 시작 위치 위쪽으로 회전
        val matrix = Matrix()
        matrix.setRotate(-90f, centerX, centerY)
        sweepGradient.setLocalMatrix(matrix)

        paint.shader = sweepGradient
        canvas.drawArc(rectF, -90f, progressAngle, true, paint)
    }

    fun startTimer(duration: Long) {
        val animator = ValueAnimator.ofFloat(360f, 0f).apply {
            this.duration = duration
            addUpdateListener {
                progressAngle = it.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }
}