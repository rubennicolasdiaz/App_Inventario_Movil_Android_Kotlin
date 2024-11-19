package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
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
    }

    private fun pasarAConsultarInventarioActivity() {
        startActivity(Intent(this, ConsultarInventarioActivity::class.java))
    }
}
