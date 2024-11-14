package com.example.indotinventario

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.example.indotinventario.databinding.ActivityConsultarInventarioBinding
import java.text.SimpleDateFormat
import java.util.*

class ConsultarInventarioActivity : AppCompatActivity() {

    // Se crea el binding para la vista:
    private lateinit var binding:ActivityConsultarInventarioBinding

    // Variable para acceder a la DB:
    private lateinit var dbInventario: DBInventario

    // Constantes y variables para permisos de cámara:
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


        // PRUEBAS///////////
        binding.tvCodigo2.setText("2000000074047")
        buscarArticulo("2000000074047")
    }

    private fun cargarVista() {

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

        binding.buttonGuardar.setOnClickListener {

            actualizarStock()
            limpiarCampos()
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
            data?.getStringExtra("codigo")?.let { codigoBarras ->

                var codigoArticulo: String = codigoBarras

                binding.tvCodigo2.setText(codigoArticulo) // Código de barras





                buscarArticulo(codigoArticulo)
            }
        }
    }

    private fun buscarArticulo(codigoBarras: String) {

        try{
            // Obtener el cursor con los datos del código de barras
            val cursor: Cursor = dbInventario.obtenerCodigoBarras(codigoBarras)

            // Verificamos si el cursor tiene resultados
            if (cursor.moveToFirst()) {
                // Obtener los valores del cursor para el código de barras
                val idArticuloIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                val idCombinacionIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)


                    val idArticulo = cursor.getString(idArticuloIndex)
                    val idCombinacion = cursor.getString(idCombinacionIndex)

                // Cerramos el cursor de código de barras
                cursor.close()

                binding.tvIdArticulo2.setText(idArticulo) // ID del artículo
                binding.tvIdCombinacion2.setText(idCombinacion) // Combinación (puede ser nulo)


                // Ahora obtenemos los detalles del artículo usando el idArticulo
                val articuloCursor: Cursor = dbInventario.obtenerArticulo(idArticulo)

                if(articuloCursor.moveToFirst()){

                    // Verificamos si se encuentra el artículo

                    val descripcionIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)
                    val stockRealIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_STOCK_REAL)

                    // Obtener los detalles del artículo
                    val descripcion = articuloCursor.getString(descripcionIndex)
                    val stockReal = articuloCursor.getInt(stockRealIndex)

                    // Cerramos el cursor de artículo
                    articuloCursor.close()

                    // Asignamos los valores a los EditText en la interfaz de usuario
                    binding.tvDescripcion2.setText(descripcion) // Descripción del artículo
                    binding.tvUnidades2.setText(stockReal.toString()) // Stock real


                    // Ahora obtenemos los detalles de la partida usando el idArticulo
                    val partidaCursor: Cursor = dbInventario.obtenerPartidaPorIdArticulo(idArticulo)

                    Log.i("Fallo Cursor Partida", "${partidaCursor.columnCount}")
                    Log.i("Fallo Cursor Partida", "${partidaCursor.count}")
                    Log.i("Fallo Cursor Partida", "${partidaCursor.columnNames}")
                    Log.i("Fallo Cursor Partida", "${partidaCursor.position}")
                    Log.i("Fallo Cursor Partida", "${partidaCursor.javaClass}")


                    // Recorremos todas las filas de partida asociadas al idArticulo
                    if(partidaCursor.moveToFirst()) {
                        Log.i("Fallo Cursor Partida", "eSTe Id Articulo tiene partida")

                        val partidaIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_PARTIDA)
                        val fechaCaducidadIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_FECHA_CADUCIDAD)
                        val numeroSerieIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_NUMERO_SERIE)

                        // Obtener los detalles de la partida:
                        val partida = partidaCursor.getString(partidaIndex)
                        val fechaCaducidad = partidaCursor.getString(fechaCaducidadIndex)
                        val numeroSerie = partidaCursor.getString(numeroSerieIndex)

                        binding.tvPartida2.setText(partida)
                        binding.tvFecha2.setText(fechaCaducidad)

                        if(!numeroSerie.isNullOrEmpty()){
                            binding.tvNumero2.setText(numeroSerie)
                        }else{

                            binding.tvNumero2.setText("")
                        }
                    }
                }
            } else {
                // Si no se encuentra el código de barras, mostramos un mensaje
                Toast.makeText(this, "Código de barras no encontrado", Toast.LENGTH_LONG).show()
            }
        }catch(e:Exception){
            Toast.makeText(this, e.message,
                Toast.LENGTH_LONG).show()
        }
    }


    // Inicializar la base de datos
        private fun inicializarDB(){

        dbInventario = DBInventario(this)
    }

    // Actualizar número de Stock:
    private fun actualizarStock(){

        try{
            val idArticulo:String = binding.tvIdArticulo2.text.toString()


            // Obtener el valor del TextView como String
            val unidadesStockString = binding.tvUnidades2.text.toString()

            // Convertirlo a Int de forma segura (en caso de que no sea un número válido)
            val unidadesStock: Int = unidadesStockString.toInt()

            val codigoBarras = binding.tvCodigo2.text.toString()

            if(codigoBarras.isEmpty()){

                Toast.makeText(this, "No hay código de barras registrado",
                    Toast.LENGTH_SHORT).show()

            }else if(idArticulo.isEmpty()){

                Toast.makeText(this, "No se puede actualizar el stock de un artículo no registrado",
                    Toast.LENGTH_SHORT).show()

            }else if(unidadesStockString.isEmpty()){

                Toast.makeText(this, "No se puede actualizar un stock vacío",
                    Toast.LENGTH_SHORT).show()

            }else{

                dbInventario.actualizarStock(idArticulo, unidadesStock)
            }
        }catch(e:Exception){
            Toast.makeText(this, "No hay ningún artículo para guardar",
                Toast.LENGTH_SHORT).show()
        }
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


    // Menú:
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.idInicio -> {

                dbInventario.close()
                finish()
                true
            }
            R.id.idSalir -> {
                dbInventario.close()
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun obtenerFechaActual(): String {

        val formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) // Definir el formato de fecha
        val fechaActual = Date() // Obtener la fecha y hora actual
        return formatoFecha.format(fechaActual) // Formatear la fecha en el formato deseado y devolverla como String
    }
}