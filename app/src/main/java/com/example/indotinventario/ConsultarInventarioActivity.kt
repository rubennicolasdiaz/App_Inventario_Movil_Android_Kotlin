package com.example.indotinventario

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.indotinventario.Pruebas.Tabla1
import com.example.indotinventario.Pruebas.Tabla2
import com.example.indotinventario.Pruebas.Tabla3
import com.example.indotinventario.Pruebas.Tabla4
import com.example.indotinventario.databinding.ActivityConsultarInventarioBinding
import com.google.gson.Gson
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
    private val listaTabla4 = ArrayList<Tabla4>()

    private var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var date: Date? = null

    private lateinit var fileTabla2: File
    private val gson = Gson()
    private var readerTabla1: Reader? = null
    private var readerTabla2: Reader? = null
    private var readerTabla3: Reader? = null

    private var t1Array: Array<Tabla1>? = null
    private var t2Array: Array<Tabla2>? = null
    private var t3Array: Array<Tabla3>? = null

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

        cargarDatosIniciales()
        cargarEventos()
        cargarSpinner()
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
                Toast.makeText(this, R.string.permiso_uso_camara, Toast.LENGTH_SHORT).show()
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
                val index = buscarCodigo(codigo, controlCodigoBarras)
                if (index != -1) {
                    cargarDatos()
                    binding.etDescripcion.setText(listaDescripcion[index])
                    binding.etUnidadesContadas.requestFocus()
                } else {
                    alertaCodigoErroneo()
                    clearFields()
                }
            }
        }
    }

    private fun clearFields() {

        binding.etDescripcion.setText("")
        binding.tvIdArticulo.setText("")
        binding.tvIdCombinacion.setText("")
        binding.tvPartida.text = ""
        binding.tvFechaCaducidad.text = ""
        binding.tvNumeroSerie.text = ""
    }

    private fun alertaCodigoErroneo() {

        AlertDialog.Builder(this)
            .setTitle("Código")
            .setMessage("El código de barras escaneado es:")
            .setMessage("$codigoBarras")
            .setPositiveButton(android.R.string.ok, null)
            .show()
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

        Toast.makeText(this, R.string.permiso_camara_denegado, Toast.LENGTH_SHORT).show()
    }

//---SPINNER------------------------------------------------------------------------------------

    private fun cargarSpinner() {
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cargarListaDescripcion())
        binding.etDescripcion.setAdapter(arrayAdapter)
        binding.etDescripcion.inputType = 0
    }

    private fun cargarListaDescripcion(): ArrayList<String> {
        return try {
            readerTabla2 = FileReader(fileTabla2)
            val acb: Array<Tabla2> = arrayOf(gson.fromJson(readerTabla2, Tabla2::class.java))

            array.clear()
            for (t2 in acb) {
                array.add(t2.descripcion.trim())
            }
            array
        } catch (e: Exception) {
            System.err.println(e)
            array
        }
    }

//---GUARDAR INFORMACION------------------------------------------------------------------------

    fun guardarUnidadesContadas(view: View) {
        val codigo = binding.etCodigoBarras.text.toString().trim()
        if (buscarCodigo(codigo, controlCodigoBarras) != -1) {
            if (binding.etUnidadesContadas.text.isEmpty()) {
                Toast.makeText(this, R.string.sin_unidades_contadas, Toast.LENGTH_SHORT).show()
            } else {
                controlUnidadesContadas[controlCodigoBarras.indexOf(codigo)] = binding.etUnidadesContadas.text.toString().trim()
                Toast.makeText(this, R.string.dato_guardado, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, R.string.sin_producto, Toast.LENGTH_SHORT).show()
        }
        binding.etUnidadesContadas.text.clear()
    }

    fun buscarCodigo(r: String, array: ArrayList<String>): Int {
        return array.indexOf(r)
    }

    fun crearJSON(view: View) {
        try {
            cargarTablas(this)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        t1Array = gson.fromJson(readerTabla1, Array<Tabla1>::class.java)
        t3Array = gson.fromJson(readerTabla3, Array<Tabla3>::class.java)

        llenarListas()
        instanciarObjetosTabla4()
        crearFechaActual()

        tablaPreparadaConJSON = gson.toJson(listaTabla4).toString()
        guardarJSON(tablaPreparadaConJSON!!, "Inventario$nombreArchivo$fechaActual.json") // Include time if needed

        limpiarListas()
    }

    private fun limpiarListas() {

        listaIdArticulo.clear()
        listaUnidadesContadas.clear()
        listaFechaCaducidad.clear()
        listaIdCombinacion.clear()
        listaNumeroSerie.clear()
        listaPartida.clear()
        listaTabla4.clear()
    }

    private fun crearFechaActual() {

        dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        date = Date()
        fechaActual = dateFormat.format(date)
    }

    private fun instanciarObjetosTabla4() {

        for (i in t1Array?.indices!!) {
            try {
                listaUnidadesContadas.add(if (controlUnidadesContadas[i].toInt() >= 0) controlUnidadesContadas[i] else "0")
            } catch (e: Exception) {
                listaUnidadesContadas.add("0")
            }

            rellenarDatosVacios()
            listaTabla4.add(Tabla4(listaIdArticulo[i], listaIdCombinacion[i], controlPartida[i], listaFechaCaducidad[i], controlNumeroSerie[i], listaUnidadesContadas[i]))
        }
    }

    private fun rellenarDatosVacios() {
        if (listaIdArticulo.size != listaIdCombinacion.size) listaIdCombinacion.add(" ")
        if (listaIdArticulo.size != controlPartida.size) controlPartida.add(" ")
        if (listaIdArticulo.size != listaFechaCaducidad.size) listaFechaCaducidad.add(" ")
        if (listaIdArticulo.size != controlNumeroSerie.size) controlNumeroSerie.add(" ")
        if (listaIdArticulo.size != listaUnidadesContadas.size) listaUnidadesContadas.add("0")
    }

    private fun llenarListas() {

        for (t1 in t1Array!!) {
            listaIdArticulo.add(t1.idArticulo.trim())
            listaIdCombinacion.add(t1.idCombinacion.trim())
        }

        for (t3 in t3Array!!) {
            listaNumeroSerie.add(t3.nSerie.trim())
            listaPartida.add(t3.partida)
            listaFechaCaducidad.add(t3.fcaducidad)
        }
    }

    /**
     * Method to save information in a JSON file
     * @param json -> JSON string
     * @param archivo -> File name
     * @throws IOException
     */
    fun guardarJSON(json: String, archivo: String) {
        val direccion = "${android.os.Environment.getExternalStorageDirectory()}/InventarioIndot/SalidaDatos/$nombreArchivo/"
        val af = File(direccion)

        if (!af.exists()) {
            af.mkdirs()
        }

        try {
            FileWriter(File(direccion, archivo)).use { file ->
                file.write(json)
                file.flush()
            }
        } catch (e: IOException) {
            System.err.println(e)
        }
    }
//---CARGAR DATOS DE LOS 3 JSON-----------------------------------------------------------------

    /**
     * Cargamos en memoria la informacion de los 3 archivos JSON
     * @exception FileNotFoundException
     */
    private fun cargarDatos() {
        try {
            cargarTablas(this)
            cargarTablaEnArrays()
            buscarCodigoEnTablas()
            cargarInformacionEnPantalla()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    @Throws(FileNotFoundException::class)
    private fun cargarTablas(context: Context) {

        val rutaCarpeta = File(context.filesDir, "EntradaDatos")





        fileTabla2 = File(rutaCarpeta, "Inventario$nombreArchivo.json")
        readerTabla1 = FileReader(File(rutaCarpeta, "Inventario_$nombreArchivo.cbarras.json"))
        readerTabla2 = FileReader(File(rutaCarpeta, "Inventario_$nombreArchivo.articulos.json"))
        readerTabla3 = FileReader(File(rutaCarpeta, "Inventario_$nombreArchivo.partidasnserie.json"))
    }

    private fun cargarTablaEnArrays() {
        t1Array = gson.fromJson(readerTabla1, Array<Tabla1>::class.java)
        t2Array = gson.fromJson(readerTabla2, Array<Tabla2>::class.java)
        t3Array = gson.fromJson(readerTabla3, Array<Tabla3>::class.java)
    }

    private fun buscarCodigoEnTablas() {
        codigo = when {
            binding.etCodigoBarras.text.isNotEmpty() -> buscarCodigo(binding.etCodigoBarras.text.toString().trim(), controlCodigoBarras)
            binding.etDescripcion.text.isNotEmpty() -> buscarCodigo(binding.etDescripcion.text.toString().trim(), listaDescripcion)
            else -> -1
        }
    }

    private fun cargarInformacionEnPantalla() {

        for (t1 in t1Array!!) {
            if (codigo != -1 && controlCodigoBarras[codigo].equals(t1.codigoBarras, ignoreCase = true)) {
                binding.etCodigoBarras.setText(t1.codigoBarras)
                binding.tvIdArticulo.setText(t1.idArticulo)
                binding.tvIdCombinacion.setText(t1.idCombinacion)
                auxiliarCargarDatos = t1.idArticulo
            } else {
                binding.tvIdArticulo.setText("")
                binding.tvIdCombinacion.setText("")
                binding.tvPartida.setText("")
                binding.tvFechaCaducidad.setText("")
                binding.tvNumeroSerie.setText("")
                auxiliarCargarDatos = ""
            }
        }

        for (t2 in t2Array!!) {
            if (auxiliarCargarDatos.equals(t2.idArticulo, ignoreCase = true)) {

                binding.tvIdArticulo.setText(t2.idArticulo)
                binding.tvIdCombinacion.setText(t2.idCombinacion)
            }
        }

        var i = 0
        for (t3 in t3Array!!) {

            if (auxiliarCargarDatos.equals(t3.idArticulo, ignoreCase = true)) {
                binding.tvIdArticulo.setText(t3.idArticulo)
                binding.tvFechaCaducidad.setText(t3.fcaducidad)

                binding.tvPartida.setText(if (controlPartida.isEmpty()) t3.partida else controlPartida[i])
                binding.tvNumeroSerie.setText(if (controlNumeroSerie.isEmpty()) t3.nSerie else controlNumeroSerie[i])
            }
            i++
        }
    }

    private fun cargarDatosIniciales() {
        if (datosIniciales) {
            controlCodigoBarras.clear()
            if (!datosUnidadesDespuesModificar) controlUnidadesContadas.clear()
            listaDescripcion.clear()

            try {
                cargarTablas(this)
                val aux = gson.fromJson(readerTabla1, Array<Tabla1>::class.java)
                val aux2 = gson.fromJson(readerTabla2, Array<Tabla2>::class.java)
                val aux3 = gson.fromJson(readerTabla3, Array<Tabla3>::class.java)

                for (t1 in aux) {
                    controlCodigoBarras.add(t1.codigoBarras.trim())
                    if (!datosUnidadesDespuesModificar) {
                        controlUnidadesContadas.add("0")
                    }
                }

                for (t2 in aux2) {
                    listaDescripcion.add(t2.descripcion.trim())
                    listaDescripcionEnMinuscula.add(t2.descripcion.toLowerCase().trim())
                }

                if (!datosNSerieModificados) {
                    for (t3 in aux3) {
                        controlNumeroSerie.add(t3.nSerie.trim())
                    }
                    datosNSerieModificados = false
                }

                if (!datosPartidaModificados) {
                    for (t3 in aux3) {
                        controlPartida.add(t3.partida.trim())
                    }
                    datosPartidaModificados = false
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            binding.etCodigoBarras.setText("")
            binding.etDescripcion.setText("")
            binding.tvIdArticulo.setText("")
            binding.tvIdCombinacion.setText("")
            binding.tvPartida.setText("")
            binding.tvFechaCaducidad.setText("")
            binding.tvNumeroSerie.setText("")
            binding.etUnidadesContadas.setText("")
            datosIniciales = false
        }
    }

    fun modificarNumeroSerie(view: View) {

        if (buscarCodigo(binding.etCodigoBarras.text.toString().trim(), controlCodigoBarras) != -1) {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setMessage("¿Deseas modificar el numero de serie?")
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sí") { dialog, _ ->
                val mndsa = Intent(applicationContext, ModificarNumeroDeSerieActivity::class.java).apply {
                    putStringArrayListExtra("TablaNS", controlNumeroSerie)
                    putStringArrayListExtra("TablaP", controlPartida)
                    putExtra("Tabla", nombreArchivo)
                    putExtra("NS", binding.tvNumeroSerie.text.toString().trim())
                    putStringArrayListExtra("CUC", controlUnidadesContadas)
                }
                startActivity(mndsa)
                dialog.dismiss()
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
        } else {
            Toast.makeText(this, "Por favor, introduce un producto", Toast.LENGTH_SHORT).show()
        }
    }

    fun modificarPartida(view: View) {

        if (buscarCodigo(binding.etCodigoBarras.text.toString().trim(), controlCodigoBarras) != -1) {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setMessage("¿Deseas modificar la partida?")
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sí") { dialog, _ ->
                val mp = Intent(applicationContext, ModificarPartidaActivity::class.java).apply {
                    putStringArrayListExtra("TablaP", controlPartida)
                    putStringArrayListExtra("TablaNS", controlNumeroSerie)
                    putExtra("Tabla", nombreArchivo)
                    putExtra("P", binding.tvPartida.text.toString().trim())
                    putStringArrayListExtra("CUC", controlUnidadesContadas)
                }
                startActivity(mp)
                dialog.dismiss()
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
        } else {
            Toast.makeText(this, "Por favor, introduce un producto", Toast.LENGTH_SHORT).show()
        }
    }



//--------EVENTOS-------------------------------------------------------------------------------

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
    }

    private fun AlertaCodigoErroneo() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Error")
        alertDialog.setMessage("El código de barras no existe")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    //---CARPETAS Y ALMACENAMIENTO---
    private fun crearCarpetas() {

        val carpetaFicheros = File(Environment.getExternalStorageDirectory(), "InventarioIndot/EntradaDatos")
        if (!carpetaFicheros.exists()) {
            carpetaFicheros.mkdirs()
        }else{
            Toast.makeText(this, "La carpeta de archivos ya existía previamente", Toast.LENGTH_SHORT).show()
        }
    }


    fun consultarStock(view: View) {
        if (nombreArchivo != null) {
            val cia = Intent(applicationContext, ConsultarInventarioActivity::class.java)
            val datos = Bundle().apply {
                putString("BANDERA_MENUS", "0")
                putString("Tabla", nombreArchivo)
            }
            cia.putExtras(datos)
            startActivity(cia)
        } else {
            Toast.makeText(this, R.string.sin_inventario, Toast.LENGTH_SHORT).show()
        }
    }

//---MENÚ 3 PUNTITOS

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.idInicio -> {
                val ma = Intent(applicationContext, MenuActivity::class.java).apply {
                    putExtra("Tabla", nombreArchivo)
                }
                startActivity(ma)
                return true
            }

            R.id.idActualizarInventario -> {
                val cj = Intent(applicationContext, ActualizarInventarioActivity::class.java).apply {
                    putExtra("Tabla", nombreArchivo)
                }
                startActivity(cj)
                return true
            }

            R.id.idSalir -> {
                finishAffinity()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
