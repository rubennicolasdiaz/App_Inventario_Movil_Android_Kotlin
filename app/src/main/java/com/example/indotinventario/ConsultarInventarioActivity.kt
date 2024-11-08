package com.example.indotinventario

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.indotinventario.databinding.ActivityConsultarInventarioBinding
import java.text.SimpleDateFormat
import java.util.*

class ConsultarInventarioActivity : AppCompatActivity() {

    // Se crea el binding para la vista:
    private lateinit var binding:ActivityConsultarInventarioBinding

    // Variable para acceder a la DB:
    private lateinit var dbInventario: DBInventario

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
    companion object {
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

        // Se verifican los permisos de cámara
        verificarYPedirPermisosDeCamara()

        // Se inicializa base de datos:
        inicializarDB()
    }

    private fun cargarVista() {

        binding.tvUnidades2.setText("0")

        binding.buttonEscanear.setOnClickListener {
            if (!permisoCamaraConcedido) {
                Toast.makeText(this, "Permiso de cámara no concedido", Toast.LENGTH_SHORT).show()
                permisoSolicitadoDesdeBoton = true
                verificarYPedirPermisosDeCamara()
                return@setOnClickListener
            }
            escanear()
        }

        binding.buttonLimpiar.setOnClickListener{

            limpiarCampos()
        }

        binding.buttonIncrementar.setOnClickListener {
            incrementarUnidades()
        }

        binding.buttonDisminuir.setOnClickListener {
            disminuirUnidades()
        }
    }

    private fun disminuirUnidades() {

        val currentValue = binding.tvUnidades2.text.toString()

        // Verificar si el texto es un número válido
        val unidades = if (currentValue.isNotEmpty()) {
            currentValue.toIntOrNull() ?: 0 // Si no es un número válido, usar 0
        } else {
            0
        }

        // Disminuir la cantidad, asegurándonos de que no sea menor que 0
        val newValue = if (unidades > 0) unidades - 1 else 0

        // Actualizar el EditText con el nuevo valor
        binding.tvUnidades2.setText(newValue.toString())
    }

    private fun incrementarUnidades() {
        val currentValue = binding.tvUnidades2.text.toString()

        // Verificar si el texto es un número válido
        val unidades = if (currentValue.isNotEmpty()) {
            currentValue.toIntOrNull() ?: 0 // Si no es un número válido, usar 0
        } else {
            0
        }

        // Incrementar la cantidad
        val newValue = unidades + 1

        // Actualizar el EditText con el nuevo valor
        binding.tvUnidades2.setText(newValue.toString())
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

                var codigoArticulo: String = codigo
                //binding.etCodigo.setText(codigo)

                binding.tvCodigo2.setText(codigoArticulo) // Código de barras

                buscarArticulo(codigoArticulo)

            }
        }
    }

    private fun buscarArticulo(codigoBarras: String) {
        // Obtener el cursor con los datos del código de barras
        val cursor: Cursor? = dbInventario.obtenerCodigoBarras(codigoBarras)

        // Verificamos si el cursor tiene resultados
        if (cursor != null && cursor.moveToFirst()) {
            // Obtener los valores del cursor para el código de barras
            val idArticuloIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
            val idCombinacionIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)

            // Verificamos si las columnas existen
            if (idArticuloIndex != -1 && idCombinacionIndex != -1) {
                val idArticulo = cursor.getString(idArticuloIndex)
                val idCombinacion = cursor.getString(idCombinacionIndex)

                // Ahora obtenemos los detalles del artículo usando el idArticulo
                val articuloCursor: Cursor? = dbInventario.obtenerArticulo(idArticulo)

                // Verificamos si se encuentra el artículo
                if (articuloCursor != null && articuloCursor.moveToFirst()) {
                    val descripcionIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)
                    val stockRealIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_STOCK_REAL)

                    // Verificamos si las columnas existen en el cursor de artículo
                    if (descripcionIndex != -1 && stockRealIndex != -1) {
                        // Obtener los detalles del artículo
                        val descripcion = articuloCursor.getString(descripcionIndex)
                        val stockReal = articuloCursor.getInt(stockRealIndex)

                        // Asignamos los valores a los EditText en la interfaz de usuario


                        binding.tvCodigo2.setText(codigoBarras) // Código de barras
                        binding.tvDescripcion2.setText(descripcion) // Descripción del artículo
                        binding.tvIdArticulo2.setText(idArticulo) // ID del artículo
                        binding.tvIdCombinacion2.setText(idCombinacion) // Combinación (puede ser nulo)
                        binding.tvUnidades2.setText(stockReal.toString()) // Stock real

                    } else {
                        // Si no se encuentran las columnas de descripción o stock real, mostramos un error
                        Toast.makeText(this, "Datos del artículo incompletos.", Toast.LENGTH_LONG).show()
                    }

                    // Cerramos el cursor de artículo
                    articuloCursor.close()
                } else {
                    // Si no se encuentra el artículo, mostramos un mensaje
                    Toast.makeText(this, "Artículo no encontrado.", Toast.LENGTH_LONG).show()
                }
            } else {
                // Si no se encuentran las columnas necesarias en el cursor de código de barras, mostramos un error
                Toast.makeText(this, "Datos del código de barras incompletos.", Toast.LENGTH_LONG).show()
            }

            // Cerramos el cursor de código de barras
            cursor.close()
        } else {
            // Si no se encuentra el código de barras, mostramos un mensaje
            Toast.makeText(this, "Código de barras no encontrado.", Toast.LENGTH_LONG).show()
        }
    }

    // Inicializar la base de datos
        private fun inicializarDB(){
            dbInventario = DBInventario(this)

        // Inserción de artículos
            dbInventario.insertarArticulo("0330", "SERVIDOR HP COMPAQ PROLIANT ML30 Gen9", 1, "COMB01")
            dbInventario.insertarArticulo("033024", "SERVIDOR HP COMPAQ PROLIANT ML30 Gen9 SSD Pro", 1, "COMB02")
            dbInventario.insertarArticulo("0701", "ORDENADOR CENTURION INTEL I3 PRO", 2, "COMB06")
            dbInventario.insertarArticulo("0702", "ORDENADOR CENTURION INTEL I3 PRO", 19, "COMB07")
            dbInventario.insertarArticulo("0800", "ORDENADOR CENTURION INTEL I5 PRO", 2, "COMB08")
            dbInventario.insertarArticulo("0802", "ORDENADOR CENTURION INTEL I5 PRO", 1, "COMB09")
            dbInventario.insertarArticulo("09002", "ORDENADOR INTEL I7 PRO", 1, "COMB11")
            dbInventario.insertarArticulo("15892223", "ADAPTADOR DE VIDEO HDMI-M A DVI-H", 1, "COMB14")
            dbInventario.insertarArticulo("172253351", "LATIGUILLO RJ45 CAT.5E 1M LATIGUILLO RJ45 CAT.5E 1M", 18, "COMB15")
            dbInventario.insertarArticulo("172253352", "LATIGUILLO RJ45 CAT5.E 2 M LATIGUILLO RJ45 CAT5.E 2 M", 31, "COMB16")
            dbInventario.insertarArticulo("172253353", "LATIGUILLO RJ45 CAT5.E 3 M LATIGUILLO RJ45 CAT5.E 3 M", 29, "COMB17")
            dbInventario.insertarArticulo("1811000069", "IMPRESORA COLOR HP OFFICEJET PRO 8210", 1, "COMB18")
            dbInventario.insertarArticulo("18120001008", "TONER XEROX TK-130 KYOCERA FS 1028/1128/1300/1350/D/DN", 11, "COMB19")
            dbInventario.insertarArticulo("181200031", "CARTUCHO TINTA NEGRO HP 901 HP OFFICEJET J4580/4660/4680", 1, "COMB20")
            dbInventario.insertarArticulo("18130037", "TONER FOTOCOPIADORA KYOCERA FS3830n /3820N", 1, "COMB21")
            dbInventario.insertarArticulo("1829556321", "TONER TK-1140 KYOCERA FS1035/1135/M2035/M2535", 2, "COMB22")
            dbInventario.insertarArticulo("20668", "LOGITECH B100 BLACK", 6, "COMB23")
            dbInventario.insertarArticulo("22264", "CUOTA POWER BI", 3, "COMB24")
            dbInventario.insertarArticulo("23390", "TELEVISION LED SAMSUNG UE55TU7005 55\" CRYSSTAL", 2, "COMB25")
            dbInventario.insertarArticulo("40020702", "TONER LASER BROTHER TN 2010", 1, "COMB26")

    // Inserción de códigos de barras
            dbInventario.insertarCodigoBarras("0000000330", "0330", "COMB01")
            dbInventario.insertarCodigoBarras("000000033024", "033024", "COMB02")
            dbInventario.insertarCodigoBarras("000000033025", "033025", "COMB03")
            dbInventario.insertarCodigoBarras("00000003330", "03330", "COMB04")
            dbInventario.insertarCodigoBarras("0000000700", "0700", "COMB05")
            dbInventario.insertarCodigoBarras("0000000701", "0701", "COMB06")
            dbInventario.insertarCodigoBarras("0000000702", "0702", "COMB07")
            dbInventario.insertarCodigoBarras("0000000800", "0800", "COMB08")
            dbInventario.insertarCodigoBarras("0000000802", "0802", "COMB09")
            dbInventario.insertarCodigoBarras("0000000850659", "0850659", "COMB10")
            dbInventario.insertarCodigoBarras("00000009002", "09002", "COMB11")
        }





    private fun limpiarCampos(){

        binding.tvCodigo2.setText("")
        binding.tvDescripcion2.setText("")
        binding.tvIdArticulo2.setText("")
        binding.tvIdCombinacion2.setText("")
        binding.tvPartida2.setText("")
        binding.tvFecha2.setText("")
        binding.tvNumero2.setText("")
        binding.tvUnidades2.setText("")
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

    private fun modificarUnidades(){


    }







    private fun crearFechaActual() {

        dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        date = Date()
        fechaActual = dateFormat.format(date)
    }

    // Menú:
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.idInicio -> {
                finish()
                true
            }
            R.id.idSalir -> {
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}