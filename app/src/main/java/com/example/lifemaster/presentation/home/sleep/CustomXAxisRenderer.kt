package com.example.lifemaster.presentation.home.sleep

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * CustomXAxisRender 클래스에 대한 설명
 * MPAndroidChart는 X축 라벨에 대해 텍스트는 지원하지만, 이미지는 지원하지 않음 → 커스텀화해서 직접 Canvas에 drawable을 그려야 함
 * drawLabels: x축의 각 위치에 라벨을 그리는 메소드
    - canvas: 라벨, 아이콘이 그려지는 영역
    - pos: 라벨이 그려지는 수칙 위치(y축)
    - anchor: 텍스트 위치를 어떤 기준점으로 정렬할 것인가 (왼쪽 정렬/가운데 정렬/오른쪽 정렬)
 */
class CustomXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    transformer: Transformer,
    private val icons: Map<Float, Drawable?>
): XAxisRenderer(viewPortHandler, xAxis, transformer) {
    override fun drawLabels(canvas: Canvas, pos: Float, anchor: MPPointF?) {
        val labelRotationAngleDegrees = mXAxis.labelRotationAngle // x축 라벨 회전 각도(단위: 도)
        val positions = FloatArray(mAxis.mEntries.size*2) // mAxis.mEntries: x축 라벨의 위치 값들 → (x,y)쌍으로 저장하기 위해 2배 크기의 배열 지정. ex) [x0, y0, x1, y1]
        for(i in mXAxis.mEntries.indices) {
            positions[i*2] = mXAxis.mEntries[i] // 짝수 인덱스에 x값 저장 (y좌표는 필요x)
        }
        transformer.pointValuesToPixel(positions) // 그래프 좌표계 → 픽셀 좌표계(실제 canvas에 그려지는 위치)로 변환. 변환하기 위해 (x,y) 쌍이 필요.

        for(i in mXAxis.mEntries.indices) {
            val x = positions[i*2] // 변환된 픽셀 좌표에서 x값을 꺼내옴
            val label = mXAxis.valueFormatter.getFormattedValue(mXAxis.mEntries[i]) // Entry의 x좌표를 통해서 포멧된 라벨을 가져옴
            drawLabel(canvas, label, x, pos, anchor, labelRotationAngleDegrees) // canvas에서 label을 실제 x좌표 및 y좌표에 주어진 회전 각도로 그림

            /**
             * 실제 아이콘을 캔버스에 그리는 커스터마이징 작업
             * 위 코드는 기존에 존재하는 로직(커스터마이징 코드x)
             * mXAxis.mEntries[i] 와 icons의 key값을 동일하게 함
             * x축: 왼쪽으로 갈수록 값이 작아지고, 오른쪽으로 갈수록 커짐
             * y축: 위쪽으로 갈수록 값이 작아지고, 아래쪽으로 갈수록 커짐
             * pixel은 해상도에 따라 위치가 바뀌기 때문에, dp 단위로 리팩토링 필요
             */
            icons[mXAxis.mEntries[i]]?.let { drawable ->
                val iconSize = 35 // 단위: pixel
                val top = pos + 50 // pos는 '라벨'의 y좌표. 아이콘을 라벨의 아래에 위치시켜야 함 (단위: pixel)
                val left = (x - iconSize/2).toInt() // x는 라벨의 x좌표. 아이콘을  라벨의 정중앙에 동일하게 위치시킴 (x좌표 기준)
                drawable.setBounds(left, top.toInt(), left + iconSize, top.toInt() + iconSize) // 왼쪽, 위쪽, 오른쪽, 아래쪽 영역 설정
                drawable.draw(canvas) // 캔버스에 아이콘을 그림
            }
        }
    }
}