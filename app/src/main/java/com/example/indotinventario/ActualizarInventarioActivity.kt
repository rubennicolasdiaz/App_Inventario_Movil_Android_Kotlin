package com.example.indotinventario

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.indotinventario.databinding.ActivityActualizarInventarioBinding

class ActualizarInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActualizarInventarioBinding
    private var nombreArchivo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implementación de View Binding:
        binding = ActivityActualizarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val datos = intent.extras
        nombreArchivo = datos?.getString("Tabla")
    }

    //---MENú

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.idInicio -> {
                val ma = Intent(applicationContext, MenuActivity::class.java)
                val datos = Bundle().apply { putString("Tabla", nombreArchivo) }
                ma.putExtras(datos)
                startActivity(ma)
                return true
            }
            R.id.idActualizarInventario -> {
                val aia = Intent(applicationContext, ActualizarInventarioActivity::class.java)
                val datos = Bundle().apply { putString("Tabla", nombreArchivo) }
                aia.putExtras(datos)
                startActivity(aia)
                return true
            }
            
            R.id.idConsultarInventario -> {
                val cia = Intent(applicationContext, ConsultarInventarioActivity::class.java)
                val datos = Bundle().apply { putString("Tabla", nombreArchivo) }
                cia.putExtras(datos)
                startActivity(cia)
                return true
            }
            R.id.idSalir -> {
                finishAffinity()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
