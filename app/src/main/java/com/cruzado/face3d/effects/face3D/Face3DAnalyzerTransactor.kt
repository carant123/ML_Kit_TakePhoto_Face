package com.cruzado.face3d.effects.face3D

import android.content.Context
import android.os.Handler
import android.util.SparseArray
import com.cruzado.face3d.overlay.GraphicOverlay
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLAnalyzer.MLTransactor
import com.huawei.hms.mlsdk.face.face3d.ML3DFace


class Face3DAnalyzerTransactor(varMGraphicOverlay: GraphicOverlay?, context: Context, handler: Handler) : MLTransactor<ML3DFace> {

    private var mGraphicOverlay: GraphicOverlay? = varMGraphicOverlay
    private var mContext: Context = context
    private var mHandler: Handler = handler
    private var  safeToTakePicture = true

    override fun transactResult(result: MLAnalyzer.Result<ML3DFace>?) {
        mGraphicOverlay!!.clear()
        val faceSparseArray: SparseArray<ML3DFace> = result?.analyseList as SparseArray<ML3DFace>

        for (i in 0 until faceSparseArray.size()) {
            val graphic = ML3DFaceGraphic(
                mGraphicOverlay!!,
                faceSparseArray.valueAt(i),
                mContext
            )
            mGraphicOverlay!!.add(graphic)

//            if (safeToTakePicture) {
//                safeToTakePicture = false
//                mHandler.sendEmptyMessage(Face3DActivity.TAKE_PHOTO)
//            }

        }
    }

    override fun destroy() {
        mGraphicOverlay?.clear()
    }

}