package com.example.lifemaster.presentation.home.pomodoro

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.animation.ValueAnimator
import androidx.core.content.ContextCompat
import com.example.lifemaster.R

class CircularTimerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progressAngle: Float = 360f // 초기 값 (완전히 채워진 상태)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width / 2f) - 10f // 원 크기 조정

        val rectF = RectF(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius
        )

        // 배경 원 (채워진 부분)
        paint.shader = null
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius, paint)

        // 진행 원 (남은 시간)
        val sweepGradient = SweepGradient(centerX, centerY, intArrayOf(ContextCompat.getColor(context, R.color.blue_100), ContextCompat.getColor(context, R.color.blue_10)), floatArrayOf(0.0f, progressAngle / 360f))

        // 시작 지점을 위쪽(-90도)으로 회전 ★★★
        val matrix = Matrix()
        matrix.setRotate(-90f, centerX, centerY) // -90도 회전 (위쪽에서 시작)
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
