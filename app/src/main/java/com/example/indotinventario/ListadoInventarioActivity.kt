package com.example.indotinventario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityListadoInventarioBinding


class ListadoInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListadoInventarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityListadoInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        cargarVista()
    }

    private fun cargarVista() {

        // Preparar el Adapter para los objetos Json que hay que pasarle
        binding.recyclerViewJson



    }
}