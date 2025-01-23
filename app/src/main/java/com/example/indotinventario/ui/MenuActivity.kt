package com.example.indotinventario.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.indotinventario.R
import com.example.indotinventario.logica.UploadJsonFile
import com.example.indotinventario.databinding.ActivityMenuBinding
import com.example.indotinventario.logica.DBInventario
import com.example.indotinventario.logica.DBUsuarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast

class MenuActivity : AppCompatActivity() {

    private lateinit var dbInventario: DBInventario
    private lateinit var dbUsuarios: DBUsuarios

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuBinding.inflate(layoutInflater) //Implementación de View Binding:
        setContentView(binding.root)

        cargarVista()
        inicializarDB()
    }

    private fun inicializarDB(){

        dbInventario = DBInventario.getInstance(this)
        dbUsuarios = DBUsuarios.getInstance(this)
    }

    private fun cargarVista() {

        binding.buttonBuscarCodigo.setOnClickListener {
            pasarABuscarCodigoBarrasActivity()
        }

        binding.buttonBuscarDescripcion.setOnClickListener {
            pasarABuscarDescripcionActivity()
        }

        binding.buttonHistorial.setOnClickListener {
            pasarAHistorialActivity()
        }

        binding.buttonGuardar.setOnClickListener {

            showAlertDialog(this)
        }
    }

    private fun pasarABuscarCodigoBarrasActivity() {
        moveTaskToBack(true)
        startActivity(Intent(this, BuscarCodigoBarrasActivity::class.java))
    }

    private fun pasarABuscarDescripcionActivity() {
        moveTaskToBack(true)
        startActivity(Intent(this, BuscarDescripcionActivity::class.java))
    }

    private fun pasarAHistorialActivity() {
        moveTaskToBack(true)
        startActivity(Intent(this, HistorialActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Se sobreescribe el menú de los 3 puntitos

        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.idSalir -> {
                showAlertDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAlertDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar salida")
            .setMessage("¿Estás seguro de que quieres guardar los cambios del " +
                    "inventario en la nube y salir de la app?")

            .setPositiveButton("Sí") { dialog, which ->

                dbInventario.close()

                // Se llama a corrutina para salvar el inventario de la DB a un fichero Json en almacenamiento externo:
                lifecycleScope.launch(Dispatchers.IO){

                    if(dbInventario.obtenerTodosItemInventario().count <= 0){

                        withContext(Dispatchers.Main){
                            MotionToast.createToast(this@MenuActivity,
                                "SIN UNIDADES GUARDADAS",
                                "No se han guardado unidades de ningún artículo",
                                MotionToast.TOAST_WARNING,
                                MotionToast.GRAVITY_CENTER,
                                MotionToast.SHORT_DURATION,
                                null)
                        }
                    }else{

                        async{UploadJsonFile.uploadJsonInventario(this@MenuActivity, dbInventario)}.await()

                        if(UploadJsonFile.isUploadOK()){

                            finishAffinity() // Finaliza la app.
                        }else {
                            withContext(Dispatchers.Main) {
                                MotionToast.createToast(
                                    this@MenuActivity,
                                    "ERROR DE SUBIDA DE FICHERO",
                                    "Revisar conexión a Internet o servicio web",
                                    MotionToast.TOAST_ERROR,
                                    MotionToast.GRAVITY_CENTER,
                                    MotionToast.SHORT_DURATION,
                                    null
                                )
                            }
                        }
                    }
                }
            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
    }
}
