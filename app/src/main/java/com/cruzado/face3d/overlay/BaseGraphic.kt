package com.cruzado.face3d.overlay

import android.graphics.Canvas
import com.huawei.hms.mlsdk.common.LensEngine


abstract class BaseGraphic(val mOverlay: GraphicOverlay) {

    abstract fun draw(canvas: Canvas)

    fun scaleX(x: Float): Float {
        return x * mOverlay.mWidthScaleFactor
    }

    fun unScaleX(horizontal: Float): Float {
        return horizontal / mOverlay.mWidthScaleFactor
    }

    fun scaleY(y: Float): Float {
        return y * mOverlay.mHeightScaleFactor
    }
    fun unScaleY(vertical: Float): Float {
        return vertical / mOverlay.mHeightScaleFactor
    }

    fun translateX(x: Float): Float {
        return if (mOverlay.mFacing == LensEngine.FRONT_LENS) {
            mOverlay.width - scaleX(x)
        } else {
            scaleX(x)
        }
    }

    fun translateY(y: Float): Float {
        return scaleY(y)
    }

    open fun postInvalidate() {
        mOverlay.postInvalidate()
    }

}
