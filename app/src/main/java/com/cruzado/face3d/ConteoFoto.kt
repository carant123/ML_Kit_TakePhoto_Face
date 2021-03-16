package com.cruzado.face3d

import android.os.CountDownTimer
import com.huawei.hms.mlsdk.face.MLFace

class ConteoFoto : CountDownTimer {

    var startTime: Long? = null
    var intervalTime: Long? = null
    var interfaceCF: InterfaceConteoFoto? = null
    var flag: Int = 0

    constructor(millisInFuture: Long, countDownInterval: Long, interfaceTakeFoto: InterfaceConteoFoto) : super(millisInFuture, countDownInterval) {
        startTime = millisInFuture
        intervalTime = countDownInterval
        interfaceCF = interfaceTakeFoto
    }

    override fun onFinish() {
        flag = 0
        interfaceCF?.tomarFoto()
    }

    override fun onTick(millisUntilFinished: Long) {
        val j2: Long? = startTime?.minus(millisUntilFinished)
        if(j2!! <= 1000 && flag == 0){
            flag = 1
            interfaceCF?.mostrarConteo("OK")
        }
    }

    interface InterfaceConteoFoto {
        fun tomarFoto()
        fun mostrarConteo(conteo: String)
        fun iniciar(facePuntos: MLFace)
    }

}