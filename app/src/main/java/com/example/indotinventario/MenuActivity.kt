package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarVista()
    }






    private fun cargarVista() {

        binding.buttonBuscarCodigo.setOnClickListener {
            pasarABuscarCodigoBarrasActivity()
        }

        binding.buttonBuscarDescripcion.setOnClickListener {
            pasarABuscarDescripcionActivity()
        }
    }

    private fun pasarABuscarCodigoBarrasActivity() {
        startActivity(Intent(this, BuscarCodigoBarrasActivity::class.java))
    }

    private fun pasarABuscarDescripcionActivity() {
        moveTaskToBack(true)
        startActivity(Intent(this, BuscarDescripcionActivity::class.java))
    }

    // Menú:
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.idSalir -> {


               /* dbInventario.close()


                // Se llama a corrutina para salvar los datos de la DB a ficheros Json en almacenamiento externo:
                lifecycleScope.launch(Dispatchers.IO){

                    // Se llama a la función async y al método await para que no se ejecute el
                    // siguiente código hasta que finalice la tarea anterior:
                    async{saveJsonArticulos(this@BuscarCodigoBarrasActivity)}.await()
                    async{saveJsonCodigosBarras(this@BuscarCodigoBarrasActivity)}.await()
                    async{saveJsonPartidas(this@BuscarCodigoBarrasActivity)}.await()

                    finishAffinity()
                } */

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
