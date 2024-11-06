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

    // Inicializar elementos gráficos:
    private fun cargarVista() {

        binding.buttonConsultar.setOnClickListener {
            pasarAConsultarInventarioActivity()

        }

        binding.buttonAgregar.setOnClickListener {
            pasarAInsertarArticuloActivity()
        }
    }

    // MENÚ 3 PUNTITOS
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_solo_salir, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.idSalir -> {
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun pasarAConsultarInventarioActivity() {
        startActivity(Intent(this, ConsultarInventarioActivity::class.java))
    }

    private fun pasarAInsertarArticuloActivity() {
        startActivity(Intent(this, InsertarArticuloActivity::class.java))
    }
}
