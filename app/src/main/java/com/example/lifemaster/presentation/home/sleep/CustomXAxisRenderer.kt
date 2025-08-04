package com.example.lifemaster.presentation.home.sleep

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

class CustomXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    transformer: Transformer,
    private val icons: Map<Float, Drawable?>
): XAxisRenderer(viewPortHandler, xAxis, transformer) {
    override fun drawLabels(canvas: Canvas, pos: Float, anchor: MPPointF?) {
        val labelRotationAngleDegrees = mXAxis.labelRotationAngle
        val positions = FloatArray(mAxis.mEntries.size*2)
        for(i in mXAxis.mEntries.indices) {
            positions[i*2] = mXAxis.mEntries[i]
        }
        transformer.pointValuesToPixel(positions)

        for(i in mXAxis.mEntries.indices) {
            val x = positions[i*2]
            val label = mXAxis.valueFormatter.getFormattedValue(mXAxis.mEntries[i])
            drawLabel(canvas, label, x, pos, anchor, labelRotationAngleDegrees)

            icons[mXAxis.mEntries[i]]?.let { drawable ->
                val iconSize = 35
                val top = pos + 50
                val left = (x - iconSize/2).toInt()
                drawable.setBounds(left, top.toInt(), left + iconSize, top.toInt() + iconSize)
                drawable.draw(canvas)
            }
        }
    }
}