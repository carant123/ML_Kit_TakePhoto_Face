package com.cruzado.face3d.effects.face

import android.content.Context
import android.media.FaceDetector
import android.os.Handler
import com.cruzado.face3d.ConteoFoto
import com.cruzado.face3d.overlay.GraphicOverlay
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.face.MLFace

class FaceAnalyzerTransactor(
    var mGraphicOverlay: GraphicOverlay,
    var mContext: Context,
    var interFoto: ConteoFoto.InterfaceConteoFoto,
    var mHandler: Handler
) : MLAnalyzer.MLTransactor<MLFace> {

    private var  safeToTakePicture = true

    override fun transactResult(result: MLAnalyzer.Result<MLFace>?) {
        mGraphicOverlay.clear()

        val faceSparseArray = result!!.analyseList
        for (i in 0 until faceSparseArray.size()) {
            val graphic = MLFaceGraphic(mGraphicOverlay, faceSparseArray.valueAt(i), mContext)
            var facePuntos = faceSparseArray.valueAt(i)
            mGraphicOverlay.add(graphic)

            if (safeToTakePicture) {
                safeToTakePicture = false
                interFoto.iniciar(facePuntos)
            }

        }

    }

    override fun destroy() {
        mGraphicOverlay.clear()
    }


}