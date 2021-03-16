package com.cruzado.face3d.effects.face

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.huawei.hms.mlsdk.face.MLFace

class DrawRect(context: Context, var face: Rect) : View(context) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val box = face
        canvas?.drawRect(box, obtenerBoxPaint())

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