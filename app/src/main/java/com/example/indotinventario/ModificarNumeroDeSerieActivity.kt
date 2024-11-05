package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityModificarNumeroDeSerieBinding

class ModificarNumeroDeSerieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModificarNumeroDeSerieBinding

    private val BANDERA_MODIFICAR_NUMERO_DE_SERIE = "BANDERA_MODIFICAR_NUMERO_DE_SERIE"
    private val BANDERA_MODIFICAR_NUMERO_DE_SERIE_VOLVER = "BANDERA_MODIFICAR_NUMERO_DE_SERIE_VOLVER"

    private var numeroSerieActual: String? = null
    private var numeroSerieNuevo: String? = null
    private var nombreArchivo: String? = null
    private val listaNumeroSerie = ArrayList<String>()
    private val listaPartida = ArrayList<String>()
    private val listaNumeroSerieSinDuplicados = ArrayList<String>()
    private val controlUnidadesContadas = ArrayList<String>()

    private lateinit var tvActualNumeroDeSerie: TextView
    private lateinit var etNuevoNumeroDeSerie: EditText
    private lateinit var spNumeroDeSerie: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implementación de View Binding:
        binding = ActivityModificarNumeroDeSerieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val datos = intent.extras

        datos?.let {
            numeroSerieActual = it.getString("NS")
            listaNumeroSerie.addAll(it.getStringArrayList("TablaNS") ?: arrayListOf())
            listaPartida.addAll(it.getStringArrayList("TablaP") ?: arrayListOf())
            nombreArchivo = it.getString("Tabla")
            controlUnidadesContadas.addAll(it.getStringArrayList("CUC") ?: arrayListOf())
        }

        listaNumeroSerieSinDuplicados.addAll(listaNumeroSerie.distinct().sorted())

        cargarVista()
        cambiarNombre()
        cargarSpinner()
    }

    private fun cargarVista() {
        tvActualNumeroDeSerie = findViewById(R.id.tvActualPartida)
        etNuevoNumeroDeSerie = findViewById(R.id.etNuevaPartida)
        spNumeroDeSerie = findViewById(R.id.spPartida)
    }

    // Cambiar el nombre del TextView
    private fun cambiarNombre() {
        tvActualNumeroDeSerie.text = numeroSerieActual
    }

    // Cargar el Spinner
    private fun cargarSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaNumeroSerieSinDuplicados)
        spNumeroDeSerie.adapter = adapter
    }

    // Botón para guardar y salir
    fun guardarSalir(view: View) {
        numeroSerieNuevo = etNuevoNumeroDeSerie.text.toString().trim()

        if (!numeroSerieNuevo!!.isEmpty()) {
            listaNumeroSerie[listaNumeroSerie.indexOf(numeroSerieActual)] = numeroSerieNuevo!!

            val cia = Intent(applicationContext, ConsultarInventarioActivity::class.java)
            val datos = Bundle().apply {
                putString("BANDERA_MODIFICAR_NUMERO_DE_SERIE", BANDERA_MODIFICAR_NUMERO_DE_SERIE)
                putStringArrayList("TablaNS", listaNumeroSerie)
                putStringArrayList("TablaP", listaPartida)
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
            putString("BANDERA_MODIFICAR_NUMERO_DE_SERIE_VOLVER", BANDERA_MODIFICAR_NUMERO_DE_SERIE_VOLVER)
            putString("Tabla", nombreArchivo)
            putStringArrayList("TablaNS", listaNumeroSerie)
            putStringArrayList("TablaP", listaPartida)
            putStringArrayList("CUC", controlUnidadesContadas)
        }
        cia.putExtras(datos)

        startActivity(cia)
    }
}
