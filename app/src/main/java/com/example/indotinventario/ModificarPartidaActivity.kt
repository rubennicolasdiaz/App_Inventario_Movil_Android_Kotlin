package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityModificarPartidaBinding

class ModificarPartidaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModificarPartidaBinding

    private val BANDERA_MODIFICAR_PARTIDA = "BANDERA_MODIFICAR_PARTIDA"
    private val BANDERA_MODIFICAR_PARTIDA_VOLVER = "BANDERA_MODIFICAR_PARTIDA_VOLVER"

    private var partidaActual: String? = null
    private var nuevoNumeroPartida: String? = null
    private var nombreArchivo: String? = null
    private val listaPartida = ArrayList<String>()
    private val listaNumeroSerie = ArrayList<String>()
    private val listaPartidaSinDuplicados = ArrayList<String>()
    private val controlUnidadesContadas = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implementación de View Binding:
        binding = ActivityModificarPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val datos = intent.extras

        datos?.let {
            partidaActual = it.getString("P")
            listaPartida.addAll(it.getStringArrayList("TablaP") ?: arrayListOf())
            nombreArchivo = it.getString("Tabla")
            controlUnidadesContadas.addAll(it.getStringArrayList("CUC") ?: arrayListOf())
            listaNumeroSerie.addAll(it.getStringArrayList("TablaNS") ?: arrayListOf())
        }

        listaPartidaSinDuplicados.addAll(listaPartida.distinct().sorted())

        cargarView()


    }

    private fun cargarView() {

        binding.tvActualPartida.text = partidaActual
        binding.etNuevaPartida

        // Cargar el spinner:
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaPartidaSinDuplicados)
        binding.spPartida.adapter = adapter
    }

    // Botón para guardar y salir
    fun guardarSalir(view: View) {
        nuevoNumeroPartida = binding.etNuevaPartida.text.toString().trim()

        if (!nuevoNumeroPartida!!.isEmpty()) {
            listaPartida[listaPartida.indexOf(partidaActual)] = nuevoNumeroPartida!!

            val cia = Intent(applicationContext, ConsultarInventarioActivity::class.java)
            val datos = Bundle().apply {
                putString("BANDERA_MODIFICAR_PARTIDA", BANDERA_MODIFICAR_PARTIDA)
                putStringArrayList("TablaP", listaPartida)
                putStringArrayList("TablaNS", listaNumeroSerie)
                putString("Tabla", nombreArchivo)
                putStringArrayList("CUC", controlUnidadesContadas)
            }
            cia.putExtras(datos)

            startActivity(cia)
        } else {
            Toast.makeText(this, "No puedes guardar información vacía", Toast.LENGTH_SHORT).show()
        }
    }

    // Botón para volver
    fun volver(view: View) {
        val cia = Intent(applicationContext, ConsultarInventarioActivity::class.java)
        val datos = Bundle().apply {
            putString("BANDERA_MODIFICAR_PARTIDA_VOLVER", BANDERA_MODIFICAR_PARTIDA_VOLVER)
            putString("Tabla", nombreArchivo)
            putStringArrayList("TablaP", listaPartida)
            putStringArrayList("TablaNS", listaNumeroSerie)
            putStringArrayList("CUC", controlUnidadesContadas)
        }
        cia.putExtras(datos)

        startActivity(cia)
    }
}
