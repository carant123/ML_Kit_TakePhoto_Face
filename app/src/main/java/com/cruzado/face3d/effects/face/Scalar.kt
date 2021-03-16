package com.cruzado.face3d.effects.face

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.cruzado.face3d.overlay.BaseGraphic
import com.cruzado.face3d.overlay.GraphicOverlay
import com.huawei.hms.mlsdk.face.MLFace

class Scalar(var overlay: GraphicOverlay, var context: Context, var rect: Rect, var face: MLFace) : BaseGraphic(overlay) {

    override fun draw(canvas: Canvas) {
        canvas.drawRect(translateRect(face.border), obtenerBoxPaint())
    }

    fun translateRect(rect: Rect): Rect {
        var left = translateX(rect.left.toFloat())
        var right = translateX(rect.right.toFloat())
        var bottom = translateY(rect.bottom.toFloat())
        var top = translateY(rect.top.toFloat())
        if (left > right) {
            val size = left
            left = right
            right = size
        }
        if (bottom < top) {
            val size = bottom
            bottom = top
            top = size
        }
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    fun dp2px(context: Context, dipValue: Float) : Float {
        return dipValue * context.resources.displayMetrics.density + 0.5f
    }

    private fun obtenerBoxPaint(): Paint {
        var boxPaint = Paint()
        boxPaint.color = Color.parseColor("#ffcc66")
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = dp2px(context, 1f)
        return boxPaint
    }

}