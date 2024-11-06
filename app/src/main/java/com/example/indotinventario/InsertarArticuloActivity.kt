package com.example.indotinventario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityInsertarArticuloBinding

class InsertarArticuloActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsertarArticuloBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityInsertarArticuloBinding.inflate(layoutInflater)
        setContentView(binding.root)




    }
}