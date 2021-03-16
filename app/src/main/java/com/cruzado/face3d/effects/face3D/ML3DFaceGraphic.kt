package com.cruzado.face3d.effects.face3D

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.cruzado.face3d.overlay.BaseGraphic
import com.cruzado.face3d.overlay.GraphicOverlay
import com.huawei.hms.mlsdk.common.MLPosition
import com.huawei.hms.mlsdk.face.face3d.ML3DFace
import java.util.*

class ML3DFaceGraphic: BaseGraphic {

    var overlay: GraphicOverlay? = null
    var keypointPaint: Paint? = null
    var boxPaint: Paint? = null
    var mLface: ML3DFace? = null
    var mContext: Context? = null
    var LINE_WIDTH: Float? = null

    constructor(overlay: GraphicOverlay,
                face: ML3DFace,
                context: Context) : super (overlay) {

        this.mContext = context
        this.mLface = face
        this.overlay = overlay
        LINE_WIDTH = dp2px(mContext!!, 3f)

        this.keypointPaint = Paint()
        this.keypointPaint?.color = Color.RED
        this.keypointPaint?.style = Paint.Style.FILL
        this.keypointPaint?.textSize = dp2px(context, 2f)

        this.boxPaint = Paint()
        this.boxPaint?.color = Color.BLUE
        this.boxPaint?.style = Paint.Style.STROKE
        this.boxPaint?.strokeWidth = LINE_WIDTH as Float
    }

    fun dp2px(context: Context, dipValue: Float) : Float {
        return dipValue * context.resources.displayMetrics.density + 0.5f
    }

    override fun draw(canvas: Canvas) {
        if(this.mLface == null) {
            return
        }
        val face3dPoints: List<MLPosition> = mLface?.get3DKeyPoints(ML3DFace.LANDMARK_FIVE) as List<MLPosition>

        var projectionMatrix = FloatArray(4 * 4)
        var viewMatrix = FloatArray(4 * 4)

        mLface?.get3DProjectionMatrix(projectionMatrix, 1f, 10f)
        mLface?.get3DViewMatrix(viewMatrix)

        var frameHeight = mOverlay?.height?.toFloat()?.let { unScaleX(it) }
        var frameWidth = mOverlay?.width?.toFloat()?.let { unScaleY(it) }

        var adaptMatrix: FloatArray = floatArrayOf(frameWidth!! /2f, 0f, frameWidth / 2f, 0f, -frameHeight!! / 2f, frameHeight / 2f, 0f, 0f, 1f)
        val face2dPoints: List<MLPosition> = this!!.translateTo2D(face3dPoints, projectionMatrix, viewMatrix, adaptMatrix)!!

        val numPaint: Paint = Paint()
        numPaint.color = Color.RED
        numPaint.textSize = frameHeight / 80f
        for (i in face2dPoints.indices) {
            val point = face2dPoints[i]
            boxPaint?.let { canvas.drawPoint(translateX(point.x.toFloat()), translateY(point.y.toFloat()), it) }
            canvas.drawText("" + i, translateX(point.x.toFloat()), translateY(point.y.toFloat()), numPaint)
        }
    }

    private fun translateTo2D(face3dPoints: List<MLPosition>, projectionMatrix: FloatArray, viewMatrix: FloatArray, adaptMatrix: FloatArray): List<MLPosition>? {
        val face2dPoints: MutableList<MLPosition> = ArrayList()
        for (i in face3dPoints.indices) {
            val curPoint = face3dPoints[i]
            val curVec = floatArrayOf(curPoint.x, curPoint.y, curPoint.z, 1f)
            //1 V*Vec
            val temp1: FloatArray = matrixMulti(viewMatrix, 4, 4, curVec)
            //2 P*(V*Vec)
            val temp2: FloatArray = matrixMulti(projectionMatrix, 4, 4, temp1)
            //3 calculations x’ y'
            val temp3 = floatArrayOf(temp2[0] / temp2[3], temp2[1] / temp2[3], 1f)
            //4 calculations X Y coordinates
            val point: FloatArray = matrixMulti(adaptMatrix, 3, 3, temp3)
            face2dPoints.add(MLPosition(point[0], point[1]))
        }
        return face2dPoints
    }

    private fun matrixMulti(V: FloatArray, m: Int, n: Int, vec: FloatArray): FloatArray {
        val result = FloatArray(n)
        for (i in 0 until n) {
            var temp = 0f
            for (j in 0 until m) {
                temp += V[i * m + j] * vec[j]
            }
            result[i] = temp
        }
        return result
    }

}