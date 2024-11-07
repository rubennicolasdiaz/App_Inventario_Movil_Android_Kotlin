package com.example.indotinventario

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result

class EscanearActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private lateinit var escanerZXing: ZXingScannerView

    override fun onCreate(state: Bundle?) {

        super.onCreate(state)
        escanerZXing = ZXingScannerView(this)

        // Hacer que el contenido de la actividad sea el escáner
        setContentView(escanerZXing)
    }

    override fun onResume() {
        super.onResume()
        // Establecer el manejador del resultado
        escanerZXing.setResultHandler(this)
        escanerZXing.startCamera() // Comenzar la cámara
    }

    override fun onPause() {
        super.onPause()
        escanerZXing.stopCamera() // Detener la cámara
    }

    // Manejar el resultado del escáner
    override fun handleResult(resultado: Result) {
        // Obtener el código/texto leído
        val codigo = resultado.text

        // Preparar un Intent para regresar datos a la actividad que nos llamó
        val intentRegreso = Intent()
        intentRegreso.putExtra("codigo", codigo)
        setResult(Activity.RESULT_OK, intentRegreso)

        // Cerrar la actividad
        finish()
    }


    // Menú:
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_volver, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.Volver -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
