package com.cruzado.face3d.effects.face

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.Log
import com.cruzado.face3d.overlay.BaseGraphic
import com.cruzado.face3d.overlay.GraphicOverlay
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceShape
import java.text.DecimalFormat
import java.util.*

class MLFaceGraphic(var overlay: GraphicOverlay, var face: MLFace, var context: Context): BaseGraphic(overlay) {

    private val TAG = MLFaceGraphic::class.java.simpleName

    private var LINE_WIDTH = dp2px(context, 1f)

    private val mFace: MLFace? = face

    private var facePositionPaint: Paint? = obtenerFacePositionPaint()
    private var keypointPaint: Paint? = obtenerKeypointPaint()
    private var boxPaint: Paint = obtenerBoxPaint()
    private var facePaint: Paint? = obtenerFacePaint()
    private var eyePaint: Paint? = obtenerEyePaint()
    private var eyebrowPaint: Paint? = obtenerEyebrowPaint()
    private var lipPaint: Paint? = obtenerLipPaint()
    private var nosePaint: Paint? = obtenerNosePaint()
    private var noseBasePaint: Paint? = obtenerNoseBasePaint()
    private val textPaint: Paint? = obtenerTextPaint()
    private var faceFeaturePaintText: Paint? = obtenerFaceFeaturePaintText()

    companion object {
        var drawReact: Rect? = null
        var overlaymain: GraphicOverlay? = null
        var mFaceMain: MLFace? = null
    }
    
    private fun obtenerTextPaint(): Paint? {
        var tPaint = Paint()
        tPaint.color = Color.WHITE
        tPaint.textSize = dp2px(context, 6f)
        tPaint.typeface = Typeface.DEFAULT
        return tPaint
    }

    private fun obtenerKeypointPaint(): Paint? {
        var keypointPaint = Paint()
        keypointPaint.color = Color.RED
        keypointPaint.style = Paint.Style.FILL
        keypointPaint.textSize = dp2px(context, 2f)
        return keypointPaint
    }

    private fun obtenerBoxPaint(): Paint {
        var boxPaint = Paint()
        boxPaint.color = Color.parseColor("#ffcc66")
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = LINE_WIDTH
        return boxPaint
    }

    private fun obtenerFacePositionPaint(): Paint? {
        var  faceFeaturePaintText = Paint()
        faceFeaturePaintText.color = Color.WHITE
        return faceFeaturePaintText
    }

    private fun obtenerFacePaint(): Paint? {
        var facePaint = Paint()
        facePaint.color = Color.parseColor("#ffcc66")
        facePaint.style = Paint.Style.STROKE
        facePaint.strokeWidth = LINE_WIDTH
        return facePaint
    }

    private fun obtenerEyePaint(): Paint? {
        var eyePaint = Paint()
        eyePaint.color = Color.parseColor("#00ccff")
        eyePaint.style = Paint.Style.STROKE
        eyePaint.strokeWidth = LINE_WIDTH
        return eyePaint
    }

    private fun obtenerEyebrowPaint(): Paint? {
        var eyebrowPaint = Paint()
        eyebrowPaint.color = Color.parseColor("#006666")
        eyebrowPaint.style = Paint.Style.STROKE
        eyebrowPaint.strokeWidth = LINE_WIDTH
        return eyebrowPaint
    }

    private fun obtenerLipPaint(): Paint? {
        var lipPaint = Paint()
        lipPaint.color = Color.parseColor("#990000")
        lipPaint.style = Paint.Style.STROKE
        lipPaint.strokeWidth = LINE_WIDTH
        return lipPaint
    }

    private fun obtenerNosePaint(): Paint? {
        var nosePaint = Paint()
        nosePaint.color = Color.parseColor("#ffff00")
        nosePaint.style = Paint.Style.STROKE
        nosePaint.strokeWidth = LINE_WIDTH
        return nosePaint
    }

    private fun obtenerNoseBasePaint(): Paint? {
        var noseBasePaint = Paint()
        noseBasePaint.color = Color.parseColor("#ff6699")
        noseBasePaint.style = Paint.Style.STROKE
        noseBasePaint.strokeWidth = LINE_WIDTH
        return noseBasePaint
    }

    private fun obtenerFaceFeaturePaintText(): Paint? {
        var faceFeaturePaintText = Paint()
        faceFeaturePaintText.color = Color.WHITE
        faceFeaturePaintText.textSize = dp2px(context, 11f)
        faceFeaturePaintText.typeface = Typeface.DEFAULT
        return faceFeaturePaintText
    }
    
    fun dp2px(context: Context, dipValue: Float) : Float {
        return dipValue * context.resources.displayMetrics.density + 0.5f
    }

    private fun isLandScape(): Boolean {
        val configuration: Configuration = this.context.resources.configuration // Get the configuration information.
        val ori = configuration.orientation // Get screen orientation.
        return ori == Configuration.ORIENTATION_LANDSCAPE
    }

    fun sortHashMap(map: HashMap<String, Float>): List<String>? {
        val entey: Set<Map.Entry<String, Float>> =
            map.entries
        val list: List<Map.Entry<String, Float>> =
            ArrayList(entey)
        Collections.sort(
            list,
            comparator
        )
        val emotions: MutableList<String> =
            ArrayList()
        for (i in 0..1) {
            emotions.add(list[i].key)
        }
        return emotions
    }

    var comparator: Comparator<Map.Entry<String?, Float>> =
        Comparator { o1, o2 ->
            if (o2.value - o1.value >= 0) {
                1
            } else {
                -1
            }
        }

    override fun draw(canvas: Canvas) {
        if (this.mFace == null) {
            return
        }
        // Draw rect of face.
        canvas.drawRect(translateRect(mFace.border), boxPaint)
        drawReact = translateRect(mFace.border)
        overlaymain = overlay
        mFaceMain = mFace

        // Draw points of face.
        paintKeyPoint(canvas)
        // Draw features of face.
        paintFeatures(canvas)
    }

    private fun paintFeatures(canvas: Canvas) {
        val start = overlay.width / 4.0f
        var x = start
        val width = overlay.width / 3f
        var y: Float
        if (isLandScape()) {
            y = if (dp2px(context, overlay.height / 8.0f) < 130) 130f else dp2px(context, overlay.height / 8.0f)
        } else {
            y = if (dp2px(context, overlay.height / 16.0f) < 350.0) 350f else dp2px(context, overlay.height / 16.0f)
            if (overlay.height > 2500) { y = dp2px(context, overlay.height / 10.0f) }
        }
        val space: Float = dp2px(context, 12f)
        Log.i(TAG, x.toString() + "," + y + "; height" + overlay.height + ",width" + overlay.width)
        val emotions = HashMap<String, Float>()
        emotions["Smiling"] = mFace!!.emotions.smilingProbability
        emotions["Neutral"] = mFace.emotions.neutralProbability
        emotions["Angry"] = mFace.emotions.angryProbability
        emotions["Fear"] = mFace.emotions.fearProbability
        emotions["Sad"] = mFace.emotions.sadProbability
        emotions["Disgust"] = mFace.emotions.disgustProbability
        emotions["Surprise"] = mFace.emotions.surpriseProbability
        val result = sortHashMap(emotions)
        val decimalFormat = DecimalFormat("0.000")
        canvas.drawText("Glass Probability: " + decimalFormat.format(
                mFace.features.sunGlassProbability.toDouble()
            ), x, y, faceFeaturePaintText!!
        )
        x += width
        val sex =
            if (mFace.features.sexProbability > 0.5f) "Female" else "Male"
        canvas.drawText("Gender: $sex", x, y, faceFeaturePaintText!!)
        y -= space
        x = start
        canvas.drawText("EulerAngleY: " + decimalFormat.format(mFace.rotationAngleY.toDouble()), x, y, faceFeaturePaintText!!)
        x += width
        canvas.drawText("EulerAngleX: " + decimalFormat.format(mFace.rotationAngleX.toDouble()), x, y, faceFeaturePaintText!!)
        y -= space
        x = start
        canvas.drawText("EulerAngleZ: " + decimalFormat.format(mFace.rotationAngleZ.toDouble()), x, y, faceFeaturePaintText!!)
        x += width
        canvas.drawText("Emotion: " + result!![0], x, y, faceFeaturePaintText!!)
        x = start
        y -= space
        canvas.drawText("Hat Probability: " + decimalFormat.format(mFace.features.hatProbability.toDouble()), x, y, faceFeaturePaintText!!)
        x += width
        canvas.drawText("Age: " + mFace.features.age, x, y, faceFeaturePaintText!!)
        y -= space
        x = start
        canvas.drawText("Moustache Probability: " + decimalFormat.format(mFace.features.moustacheProbability.toDouble()), x, y, faceFeaturePaintText!!)
        y -= space
        canvas.drawText("Right eye open Probability: " + decimalFormat.format(mFace.opennessOfRightEye().toDouble()), x, y, faceFeaturePaintText!!)
        y -= space
        canvas.drawText("Left eye open Probability: " + decimalFormat.format(mFace.opennessOfLeftEye().toDouble()), x, y, faceFeaturePaintText!!)
    }

    private fun paintKeyPoint(canvas: Canvas) {
        if (mFace!!.faceShapeList != null) {
            for (faceShape in mFace.faceShapeList) {
                if (faceShape == null) {
                    continue
                }
                val points = faceShape.points
                for (i in points.indices) {
                    val point = points[i]

                    // Puntos refrencia
//                    canvas.drawPoint(
//                        translateX(point.x.toFloat()),
//                        translateY(point.y.toFloat()),
//                        boxPaint!!
//                    )

                    if (i != points.size - 1) {
                        val next = points[i + 1]
                        if (i % 3 == 0) {
                            // Numero de cada punto
                            //canvas.drawText("" + i + 1, translateX(point.x.toFloat()), translateY(point.y.toFloat()), textPaint!!)
                        }

                        // Unir puntos

//                        getPaint(faceShape)?.let {
//                            canvas.drawLines(
//                                floatArrayOf(
//                                    translateX(point.x.toFloat()),
//                                    translateY(point.y.toFloat()),
//                                    translateX(next.x.toFloat()),
//                                    translateY(next.y.toFloat())
//                                ), it
//                            )
//                        }

                    }
                }
            }
        }

        // Puntos Rojos
        for (keyPoint in mFace.faceKeyPoints) {
            if (keyPoint != null) {
                val point = keyPoint.point
                canvas.drawCircle(
                    translateX(point.x),
                    translateY(point.y),
                    dp2px(context, 3f),
                    keypointPaint!!
                )
            }
        }

    }

    private fun getPaint(faceShape: MLFaceShape): Paint? {
        return when (faceShape.faceShapeType) {
            MLFaceShape.TYPE_LEFT_EYE, MLFaceShape.TYPE_RIGHT_EYE -> eyePaint
            MLFaceShape.TYPE_BOTTOM_OF_LEFT_EYEBROW, MLFaceShape.TYPE_BOTTOM_OF_RIGHT_EYEBROW, MLFaceShape.TYPE_TOP_OF_LEFT_EYEBROW, MLFaceShape.TYPE_TOP_OF_RIGHT_EYEBROW -> eyebrowPaint
            MLFaceShape.TYPE_BOTTOM_OF_LOWER_LIP, MLFaceShape.TYPE_TOP_OF_LOWER_LIP, MLFaceShape.TYPE_BOTTOM_OF_UPPER_LIP, MLFaceShape.TYPE_TOP_OF_UPPER_LIP -> lipPaint
            MLFaceShape.TYPE_BOTTOM_OF_NOSE -> noseBasePaint
            MLFaceShape.TYPE_BRIDGE_OF_NOSE -> nosePaint
            else -> facePaint
        }
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

}