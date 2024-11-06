package com.example.indotinventario

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.indotinventario.databinding.ActivityConsultarInventarioBinding
import com.example.indotinventario.databinding.ActivityMainBinding

class ConsultarInventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultarInventarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityConsultarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}