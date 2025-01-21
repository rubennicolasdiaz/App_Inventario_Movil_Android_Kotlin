package com.example.indotinventario.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.R
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result

class EscanearActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private lateinit var escanerZXing: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        escanerZXing = ZXingScannerView(this)
        setContentView(escanerZXing) //Hacer que el contenido de la activity sea el escáner
    }

    override fun onResume() {
        super.onResume()

        escanerZXing.setResultHandler(this)  //Establecer el manejador del resultado
        escanerZXing.startCamera() //Comenzar la cámara
    }

    override fun onPause() {
        super.onPause()
        escanerZXing.stopCamera() //Detener la cámara
    }


    override fun handleResult(resultado: Result) { //Manejar el resultado del escáner

        val codigo = resultado.text // Obtener el código/texto leído

        val intentRegreso = Intent() //Preparar un Intent para regresar datos a la actividad que nos llamó
        intentRegreso.putExtra("codigo", codigo)
        setResult(Activity.RESULT_OK, intentRegreso)

        finish() //Cerrar la actividad
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Se sobreescribe el menú de los 3 puntitos
        menuInflater.inflate(R.menu.menu_volver, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.idVolver -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
