package com.example.indotinventario

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.indotinventario.databinding.ActivityConsultarInventarioBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.Reader
import java.text.SimpleDateFormat
import java.util.*

class ConsultarInventarioActivity : AppCompatActivity() {

    // Se crea el binding para la vista:
    private lateinit var binding:ActivityConsultarInventarioBinding

    // Variables que almacenan los datos de la vista:
    private lateinit var unidadesContadas: String
    private lateinit var codigoBarras: String
    private lateinit var articulo: String
    private lateinit var combinacion: String
    private lateinit var partida: String
    private lateinit var fechaCaducidad: String
    private lateinit var numeroSerie: String
    private lateinit var descripcion: String

    // Variables donde se almacenarán los datos:
    private val array = ArrayList<String>()
    private val listaDescripcion = ArrayList<String>()
    private val listaDescripcionEnMinuscula = ArrayList<String>()
    private val listaIdArticulo = ArrayList<String>()
    private val listaIdCombinacion = ArrayList<String>()
    private val listaPartida = ArrayList<String>()
    private val listaFechaCaducidad = ArrayList<String>()
    private val listaNumeroSerie = ArrayList<String>()
    private val controlNumeroSerie = ArrayList<String>()
    private val controlPartida = ArrayList<String>()
    private val listaUnidadesContadas = ArrayList<String>()
    private val controlCodigoBarras = ArrayList<String>()
    private val controlUnidadesContadas = ArrayList<String>()

    private var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var date: Date? = null

    private lateinit var fileTabla2: File
        private var readerTabla1: Reader? = null
    private var readerTabla2: Reader? = null
    private var readerTabla3: Reader? = null



    // Initial data flags
    private var datosIniciales = true
    private var datosNSerieModificados = false
    private var datosPartidaModificados = false
    private var datosUnidadesDespuesModificar = false
    private var nombreArchivo: String? = null
    private var auxiliarCargarDatos = ""
    private var tablaPreparadaConJSON: String? = null
    private var fechaActual: String? = null

    private var codigo: Int = 0

    // Permissions variables
    private companion object {
        const val CODIGO_INTENT_ESCANEAR = 3
        const val CODIGO_PERMISOS_CAMARA = 1
    }

    private var permisoCamaraConcedido = false
    private var permisoSolicitadoDesdeBoton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Implementación de View Binding:
        binding = ActivityConsultarInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarVista()



        verificarYPedirPermisosDeCamara()

        intent.extras?.let { datos ->
            nombreArchivo = "20160109_7"
            //nombreArchivo = datos.getString("Tabla")
            controlNumeroSerie.addAll(datos.getStringArrayList("TablaNS") ?: arrayListOf())
            controlPartida.addAll(datos.getStringArrayList("TablaP") ?: arrayListOf())
            controlUnidadesContadas.addAll(datos.getStringArrayList("CUC") ?: arrayListOf())

            when {
                datos.containsKey("BANDERA_MODIFICAR_NUMERO_DE_SERIE") -> {
                    datosNSerieModificados = true
                    datosUnidadesDespuesModificar = true
                }
                datos.containsKey("BANDERA_MODIFICAR_PARTIDA") -> {
                    datosPartidaModificados = true
                    datosUnidadesDespuesModificar = true
                }
                datos.containsKey("BANDERA_MENUS") -> {
                    datosUnidadesDespuesModificar = false
                }
            }
        }

        //cargarDatosIniciales()
        //cargarEventos()

    }

    private fun cargarVista() {

        codigoBarras = binding.etCodigoBarras.text.toString()
        articulo = binding.tvIdArticulo.text.toString()
        combinacion = binding.tvIdCombinacion.text.toString()
        unidadesContadas = binding.etUnidadesContadas.text.toString()
        partida = binding.tvPartida.text.toString()
        fechaCaducidad = binding.tvFechaCaducidad.text.toString()
        numeroSerie = binding.tvNumeroSerie.text.toString()
        descripcion = binding.etDescripcion.text.toString()

        binding.buttonEscanear.setOnClickListener {
            if (!permisoCamaraConcedido) {
                Toast.makeText(this, "Permiso de cámara no concedido", Toast.LENGTH_SHORT).show()
                permisoSolicitadoDesdeBoton = true
                verificarYPedirPermisosDeCamara()
                return@setOnClickListener
            }
            escanear()
        }
    }

    //---ESCANER------------------------------------------------------------------------------------

    private fun escanear() {
        val intent = Intent(this, EscanearActivity::class.java)
        startActivityForResult(intent, CODIGO_INTENT_ESCANEAR)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_INTENT_ESCANEAR && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra("codigo")?.let { codigo ->
                binding.etCodigoBarras.setText(codigo)

            }
        }
    }

    private fun limpiarCampos() {

        binding.etDescripcion.setText("")
        binding.tvIdArticulo.setText("")
        binding.tvIdCombinacion.setText("")
        binding.tvPartida.text = ""
        binding.tvFechaCaducidad.text = ""
        binding.tvNumeroSerie.text = ""
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODIGO_PERMISOS_CAMARA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Scan directly if requested from the button
                    if (permisoSolicitadoDesdeBoton) {
                        escanear()
                    }
                    permisoCamaraConcedido = true
                } else {
                    permisoDeCamaraDenegado()
                }
            }
        }
    }

    private fun verificarYPedirPermisosDeCamara() {

        val estadoDePermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (estadoDePermiso == PackageManager.PERMISSION_GRANTED) {
            // If permission granted, set the flag to true
            permisoCamaraConcedido = true
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CODIGO_PERMISOS_CAMARA)
        }
    }

    private fun permisoDeCamaraDenegado() {

        Toast.makeText(this, "Permiso de la cámara denegado", Toast.LENGTH_SHORT).show()
    }





    private fun limpiarListas() {

        listaIdArticulo.clear()
        listaUnidadesContadas.clear()
        listaFechaCaducidad.clear()
        listaIdCombinacion.clear()
        listaNumeroSerie.clear()
        listaPartida.clear()

    }

    private fun crearFechaActual() {

        dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        date = Date()
        fechaActual = dateFormat.format(date)
    }









//--------EVENTOS-------------------------------------------------------------------------------

    /*
    private fun cargarEventos() {

        binding.etCodigoBarras.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val a = buscarCodigo(binding.etCodigoBarras.text.toString().trim(), controlCodigoBarras)

                if (a != -1) {
                    cargarDatos()
                    val b = listaDescripcion[a]
                    binding.etDescripcion.setText(b)
                    binding.etUnidadesContadas.requestFocus()
                } else {
                    if (binding.etCodigoBarras.text.toString().trim().isNotEmpty()) AlertaCodigoErroneo()

                    binding.etDescripcion.setText("")
                    binding.tvIdArticulo.setText("")
                    binding.tvIdCombinacion.setText("")
                    binding.tvPartida.setText("")
                    binding.tvFechaCaducidad.setText("")
                    binding.tvNumeroSerie.setText("")
                }
            }
        }

        binding.etCodigoBarras.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val a = buscarCodigo(binding.etCodigoBarras.text.toString().trim(), controlCodigoBarras)

                if (a != -1) {
                    cargarDatos()
                    val b = listaDescripcion[a]
                    binding.etDescripcion.setText(b)
                    binding.etUnidadesContadas.requestFocus()
                } else {

                    AlertaCodigoErroneo()

                    binding.etDescripcion.setText("")
                    binding.tvIdArticulo.setText("")
                    binding.tvIdCombinacion.setText("")
                    binding.tvPartida.setText("")
                    binding.tvFechaCaducidad.setText("")
                    binding.tvNumeroSerie.setText("")
                    binding.etCodigoBarras.requestFocus()
                }
                true
            } else {
                false
            }
        }

        binding.etDescripcion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val a = binding.etDescripcion.text.toString().trim()
                val b = buscarCodigo(a, listaDescripcionEnMinuscula)

                if (b != -1) {
                    val c = controlCodigoBarras[b]
                    binding.etCodigoBarras.setText(c)
                } else {
                    binding.etCodigoBarras.setText("")
                }

                cargarDatos()
                binding.etUnidadesContadas.requestFocus()
                true
            } else {
                false
            }
        }

        binding.etDescripcion.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.etDescripcion.showDropDown()
            if (!hasFocus) {
                val a = binding.etDescripcion.text.toString().trim()
                val b = buscarCodigo(a, listaDescripcionEnMinuscula)

                if (b != -1) {
                    val c = controlCodigoBarras[b]
                    binding.etCodigoBarras.setText(c)
                } else {
                    binding.etCodigoBarras.setText("")
                }

                cargarDatos()
                binding.etDescripcion.clearFocus()
            }
        }
    } */
}