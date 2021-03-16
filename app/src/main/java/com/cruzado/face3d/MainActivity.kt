package com.cruzado.face3d

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.FaceDetector
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import com.cruzado.face3d.base.BaseActivity
import com.cruzado.face3d.effects.face.MLFaceGraphic.Companion.drawReact
import com.cruzado.face3d.effects.face.MLFaceGraphic.Companion.mFaceMain
import com.cruzado.face3d.effects.face.MLFaceGraphic.Companion.overlaymain
import com.cruzado.face3d.effects.face.Scalar
import com.cruzado.face3d.utils.Constant
import com.huawei.hms.mlsdk.face.MLFace
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : BaseActivity() {

    private val width: Int = 500
    private val height: Int = 800

    override fun getLayout(): Int = R.layout.activity_main

    override fun inicializar(savedInstanceState: Bundle?) {
        inicializarBotones()
        verificarPermisos()
    }

    private fun inicializarBotones() {
        button_white.setOnClickListener {
            verificarPermisosCamara()
        }
    }

    private fun verificarPermisosCamara() {
        if(!tieneLocationPermisos()) {
            ActivityCompat.requestPermissions(this, LISTA_PERMISSION, Constant.COD_PERMISSION_CAMARA)
        } else {
            activarCamara()
        }
    }

    private fun activarCamara() {
        val intent = Intent(this@MainActivity, FaceActivity::class.java)
        intent.putExtra(Constant.DETECT_MODE, Constant.MOST_PEOPLE)
        startActivityForResult(intent, Constant.COD_CAMARA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constant.COD_CAMARA -> {
                    mostrarFoto(data)
                }
                else -> {
                }
            }
        }

    }

    private fun mostrarFoto(data: Intent?) {
        val url = data!!.getStringExtra("url")
        val file = File(url)

//        var left = data!!.getFloatExtra("left",0.0f).toInt()
//        var right = data!!.getFloatExtra("right",0.0f).toInt()
//        var bottom = data!!.getFloatExtra("bottom",0.0f).toInt()
//        var top = data!!.getFloatExtra("top",0.0f).toInt()

        if (file.exists()) {
            Log.d("","")
            var myBitmap: Bitmap = BitmapFactory.decodeFile(file.absolutePath)

//            var canvas = Canvas(myBitmap)
//            val boxPaint = Paint()
//            boxPaint.color = Color.parseColor("#ffcc66")
//            boxPaint.style = Paint.Style.STROKE
//            boxPaint.strokeWidth = 2.0f
//            canvas.drawCircle(50f, 50f, 30f, boxPaint)

            val mutableBitmap: Bitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true)
            //var drawReactT = DrawRect(baseContext, drawReact!!)
            var drawReactT = Scalar(overlaymain!!,baseContext, drawReact!!, mFaceMain!!)
            drawReactT.draw(Canvas(mutableBitmap))

            mensaje("width: " + mutableBitmap.width)
            mensaje("height: " + mutableBitmap.height)

            val croppedBmp: Bitmap = Bitmap.createBitmap(
                mutableBitmap,
                drawReact!!.left,
                drawReact!!.top,
                drawReact!!.right,
                drawReact!!.bottom
            )

            iv_foto.setImageBitmap(mutableBitmap)
            //iv_foto.setImageURI(Uri.parse(file.toString()))

        } else {
            Log.d("UrlFinal: ", " The file does not exist")
        }
    }

    fun transform(bitmap: Bitmap): Bitmap? {
        val f: Float
        val i: Int = this.width
        require(!(i == 0 || this.height === 0)) { "El ancho o alto no debe ser cero!" }
        val width2 = i.toFloat() / bitmap.width.toFloat()
        Log.d("tamaaa: ","" + this.height as Float)
        Log.d("tamaaa: ","" + bitmap.height.toFloat())
        val height2 = this.height as Float / bitmap.height.toFloat()
        if (width2 == height2) {
            return bitmap
        }
        val createBitmap = Bitmap.createBitmap(
            this.width,
            this.height,
            if (bitmap.config != null) bitmap.config else Bitmap.Config.ARGB_8888
        )
        val max = Math.max(width2, height2)
        var f2 = this.width as Float
        var f3 = this.height as Float
        val pointF = PointF()
        //detectFace(bitmap, pointF)
        var f4 = 0.0f
        if (width2 < height2) {
            f2 = max * bitmap.width.toFloat()
            f = getLeftPoint(this.width, f2, max * pointF.x)
        } else {
            f3 = max * bitmap.height.toFloat()
            f4 = getTopPoint(this.height, f3, max * pointF.y)
            f = 0.0f
        }
        Canvas(createBitmap).drawBitmap(
            bitmap,
            null as Rect?,
            RectF(f, f4, f2 + f, f3 + f4),
            null as Paint?
        )
        bitmap.recycle()
        return createBitmap
    }

    private fun detectFace(faces: SparseArray<MLFace>, pointF: PointF, bitmap: Bitmap) {
//        val faceDetector: FaceDetector = PicassoFaceDetector.getFaceDetector()
//        if (!faceDetector.isOperational()) {
//            pointF[(bitmap.width / 2).toFloat()] = (bitmap.height / 2).toFloat()
//            return
//        }
        //val detect: SparseArray<MLFace> = faceDetector.detect(Builder().setBitmap(bitmap).build())
        val detect: SparseArray<MLFace> = faces
        val size = detect.size()
        if (size > 0) {
            var f = 0.0f
            var f2 = 0.0f
            for (i in 0 until size) {
                val pointF2 = PointF()
                getFaceCenter(detect[detect.keyAt(i)], pointF2)
                f += pointF2.x
                f2 += pointF2.y
            }
            val f3 = size.toFloat()
            pointF[f / f3] = f2 / f3
            return
        }
        pointF[(bitmap.width / 2).toFloat()] = (bitmap.height / 2).toFloat()
    }


    private fun getFaceCenter(face: MLFace, pointF: PointF) {
        val f: Float = face.coordinatePoint.x
        val f2: Float = face.coordinatePoint.y
        pointF[f + face.width / 2.0f] = f2 + face.height / 2.0f
    }


    private fun getTopPoint(i: Int, f: Float, f2: Float): Float {
        val f3 = (i / 2).toFloat()
        if (f2 <= f3) {
            return 0.0f
        }
        return if (f - f2 <= f3) i.toFloat() - f else f3 - f2
    }

    private fun getLeftPoint(i: Int, f: Float, f2: Float): Float {
        val f3 = (i / 2).toFloat()
        if (f2 <= f3) {
            return 0.0f
        }
        return if (f - f2 <= f3) i.toFloat() - f else f3 - f2
    }



    private fun verificarPermisos() {
        if(!tieneLocationPermisos()) {
            ActivityCompat.requestPermissions(this, LISTA_PERMISSION, Constant.COD_PERMISSION_CAMARA)
        }
    }

    private fun tieneLocationPermisos() : Boolean {
        for (permisos in LISTA_PERMISSION) {
            if(baseContext?.let { ActivityCompat.checkSelfPermission(it, permisos) }
                != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}