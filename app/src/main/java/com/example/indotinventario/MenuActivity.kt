package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.indotinventario.Pruebas.SaveJsonFile
import com.example.indotinventario.databinding.ActivityMenuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    // Variable para acceder a la DB:
    private lateinit var dbInventario: DBInventario

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

            dbInventario.close()


            // Se llama a corrutina para salvar el inventario de la DB a un fichero Json en almacenamiento externo:
            lifecycleScope.launch(Dispatchers.IO){

                // Se llama a la función async y al método await para que no se ejecute el
                // siguiente código hasta que finalice la tarea anterior:
                async{ SaveJsonFile.saveJsonInventario(this@MenuActivity, dbInventario)}.await()
                finishAffinity() // Finaliza la app.
            }
        }
    }

    private fun pasarABuscarCodigoBarrasActivity() {
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

    // Menú:
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.idInicio -> {

                dbInventario.close()
                finish()
                true
            }

            R.id.idSalir -> {
                dbInventario.close()


                // Se llama a corrutina para salvar el inventario de la DB a un fichero Json en almacenamiento externo:
                lifecycleScope.launch(Dispatchers.IO){

                    // Se llama a la función async y al método await para que no se ejecute el
                    // siguiente código hasta que finalice la tarea anterior:
                    async{ SaveJsonFile.saveJsonInventario(this@MenuActivity, dbInventario)}.await()
                    finishAffinity() // Finaliza la app.
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
