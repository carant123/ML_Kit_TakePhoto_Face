package com.cruzado.face3d

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cruzado.face3d.base.BaseActivity
import com.cruzado.face3d.camera.LensEnginePreview
import com.cruzado.face3d.effects.face3D.Face3DAnalyzerTransactor
import com.cruzado.face3d.overlay.GraphicOverlay
import com.cruzado.face3d.utils.Constant
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.face.face3d.ML3DFaceAnalyzer
import com.huawei.hms.mlsdk.face.face3d.ML3DFaceAnalyzerSetting
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Face3DActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val STOP_PREVIEW = 1
        const val TAKE_PHOTO = 2
    }

    private val CAMERA_REQUEST_CODE = 101
    private var analyzer: ML3DFaceAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private var mPreview: LensEnginePreview? = null
    private var overlay: GraphicOverlay? = null
    private var lensType = LensEngine.FRONT_LENS
    private var isFront = false
    private var safeToTakePicture = false
    private val storePath = Constant.STORE_PATH
    private var restart: Button? = null
    private var detectMode = 0
    private var mensajeData = Constant.NO_INTEGRATE


    override fun getLayout(): Int = R.layout.activity_live_face_analyse

    override fun inicializar(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            lensType = savedInstanceState.getInt("lensType")
        }
        mPreview = findViewById(R.id.preview)
        overlay = findViewById(R.id.face_overlay)
        createFaceAnalyzer()
        findViewById<View>(R.id.facingSwitch).setOnClickListener(this)

        restart = findViewById(R.id.restart)
        restart?.setOnClickListener {
            devolverURLImagen()
        }
        createLensEngine()
        setupPermissions()
    }

    private fun devolverURLImagen() {
        val returnIntent = Intent()
        returnIntent.putExtra("url", mensajeData)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun createFaceAnalyzer() {

        val setting = ML3DFaceAnalyzerSetting.Factory()
            .setPerformanceType(ML3DFaceAnalyzerSetting.TYPE_SPEED)
            .setTracingAllowed(true)
            .create()

        analyzer = MLAnalyzerFactory.getInstance().get3DFaceAnalyzer(setting)
        var face3D = Face3DAnalyzerTransactor(
            overlay!!,
            this@Face3DActivity,
            mHandler
        )
        analyzer!!.setTransactor(face3D)

    }

    private fun createLensEngine() {
        val context: Context = this.applicationContext
        // Create LensEngine
        mLensEngine = LensEngine.Creator(context, analyzer).setLensType(lensType)
            .applyDisplayDimension(640, 480)
            .applyFps(25.0f)
            .enableAutomaticFocus(true)
            .create()
    }

    override fun onResume() {
        super.onResume()
        startLensEngine()
    }

    private fun startLensEngine() {
        restart!!.setVisibility(View.GONE)
        if (mLensEngine != null) {
            try {
                mPreview!!.start(mLensEngine, overlay)
                safeToTakePicture = true
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start lens engine.", e)
                mLensEngine!!.release()
                mLensEngine = null
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLensEngine != null) {
            mLensEngine!!.release()
        }
        if (analyzer != null) {
            try {
                analyzer!!.stop()
            } catch (e: IOException) {
                Log.e(TAG, "Stop failed: " + e.message)
            }
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("lensType", lensType)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onClick(v: View?) {
        isFront = !isFront
        if (isFront) {
            lensType = LensEngine.FRONT_LENS
        } else {
            lensType = LensEngine.BACK_LENS
        }
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
        startPreview(v)
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    startLensEngine()
                }
            }
        }
    }


    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                STOP_PREVIEW -> stopPreview()
                TAKE_PHOTO -> takePhoto()
                else -> {
                }
            }
        }
    }

    private fun takePhoto() {
        mLensEngine!!.photograph(null,
            LensEngine.PhotographListener { bytes ->
                mHandler.sendEmptyMessage(STOP_PREVIEW)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                saveBitmapToDisk(bitmap)
            })
    }

    fun startPreview(view: View?) {
        Log.d("mens: ","" + mensajeData);
        createFaceAnalyzer()
        mPreview!!.release()
        createLensEngine()
        startLensEngine()
    }

    private fun stopPreview() {
        restart!!.visibility = View.VISIBLE
        if (mLensEngine != null) {
            mLensEngine!!.release()
            safeToTakePicture = false
        }
        if (analyzer != null) {
            try {
                analyzer!!.stop()
            } catch (e: IOException) {
                Log.e(TAG, "Stop failed: " + e.message)
            }
        }
    }

    private fun saveBitmapToDisk(bitmap: Bitmap): String {
        val appDir = File(storePath)
        if (!appDir.exists()) {
            val res: Boolean = appDir.mkdir()
            if (!res) {
                Log.e(TAG, "saveBitmapToDisk failed")
                return ""
            }
        }
        val fileName = Constant.TITLE_SMILE + System.currentTimeMillis() + Constant.FORMAT_JPG
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            val uri: Uri = Uri.fromFile(file)
            this.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.d("absoluteFilePath: ", file.absolutePath)
        mensajeData = file.absolutePath
        return file.absolutePath
    }

}