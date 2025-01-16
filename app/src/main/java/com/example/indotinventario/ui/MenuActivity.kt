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

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    // Variable para acceder a la DB:
    private lateinit var dbInventario: DBInventario

    // Variable para acceder a la DB de Usuarios:
    private lateinit var dbUsuarios: DBUsuarios

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarVista()
        inicializarDB()
    }

    // Inicializar la base de datos
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

        binding.buttonLeerFicheros.setOnClickListener {

            pasarALeerFicherosActivity()
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

    private fun pasarALeerFicherosActivity() {

        moveTaskToBack(true)
        startActivity(Intent(this, LeerFicherosActivity::class.java))
    }

    // Menú:
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

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
                    "inventario y salir de la app?")

            .setPositiveButton("Sí") { dialog, which ->

                dbInventario.close()

                // Se llama a corrutina para llamar a la API y subir el fichero
                lifecycleScope.launch(Dispatchers.IO){

                    async{ UploadJsonFile.saveJsonInventario(this@MenuActivity, dbInventario, dbUsuarios)}.await()
                }
                finishAffinity()

            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
    }
}
