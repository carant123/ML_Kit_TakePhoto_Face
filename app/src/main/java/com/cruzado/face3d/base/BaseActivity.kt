package com.cruzado.face3d.base

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    val LISTA_PERMISSION = arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())
        inicializar(savedInstanceState)
    }

    fun mensaje(mensaje: String) {
        Toast.makeText(baseContext, mensaje, Toast.LENGTH_LONG).show()
    }

    abstract fun inicializar(savedInstanceState: Bundle?)
    abstract fun getLayout(): Int

}