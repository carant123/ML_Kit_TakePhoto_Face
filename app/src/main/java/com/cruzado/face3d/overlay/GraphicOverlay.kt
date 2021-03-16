package com.cruzado.face3d.overlay

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import java.util.*


class GraphicOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val mLock = Any()
    private var mPreviewWidth = 0
    private var mPreviewHeight = 0
    var mWidthScaleFactor = 1.0f
        private set
    var mHeightScaleFactor = 1.0f
        private set
    var mFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
        private set
    private val mGraphics: MutableList<BaseGraphic> = ArrayList()

    fun clear() {
        synchronized(mLock) { mGraphics.clear() }
        this.postInvalidate()
    }

    fun add(graphic: BaseGraphic) {
        synchronized(mLock) { mGraphics.add(graphic) }
        this.postInvalidate()
    }

    fun remove(graphic: BaseGraphic?) {
        synchronized(mLock) { mGraphics.remove(graphic) }
        this.postInvalidate()
    }

    fun setCameraInfo(width: Int, height: Int, facing: Int) {
        synchronized(mLock) {
            mPreviewWidth = width
            mPreviewHeight = height
            mFacing = facing
        }
        this.postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(mLock) {
            if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                mWidthScaleFactor =
                    canvas.width.toFloat() / mPreviewWidth.toFloat()
                mHeightScaleFactor =
                    canvas.height.toFloat() / mPreviewHeight.toFloat()
            }
            for (graphic in mGraphics) {
                graphic.draw(canvas)
            }
        }
    }
}