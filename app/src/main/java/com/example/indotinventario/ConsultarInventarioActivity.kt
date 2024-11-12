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
    }

    private fun cargarVista() {

        // val dateActual = obtenerFechaActual()


        // binding.tvUnidades2.setText("0")

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
            data?.getStringExtra("codigo")?.let { codigo ->

                var codigoArticulo: String = codigo
                //binding.etCodigo.setText(codigo)

                binding.tvCodigo2.setText(codigoArticulo) // Código de barras

                buscarArticulo(codigoArticulo)
            }
        }
    }

    private fun buscarArticulo(codigoBarras: String) {


        ////////////////TABLA CÓDIGOS BARRAS:
        // Obtener el cursor con los datos del código de barras
        val codigoBarrasCursor: Cursor = dbInventario.obtenerCodigoBarras(codigoBarras) // El cursor sólo es de la tabla códigos barras

        // Verificamos si el cursor tiene resultados (si el código de barras existe en la base de datos):
        if (codigoBarrasCursor.moveToFirst()) {

            // Se saca eñ índice de las columnas IdArtículo e IdCombinación de la tabla de Códigos de Barras:

            val idArticuloIndex = codigoBarrasCursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
            val idCombinacionIndex = codigoBarrasCursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)

            // Se sacan los valores de las columnas asociados al código de barras:
            val idArticulo = codigoBarrasCursor.getString(idArticuloIndex)
            val idCombinacion = codigoBarrasCursor.getString(idCombinacionIndex)

            // Se establece el cursor para buscar en la tabla de Artículos:
            val idArticuloCursor: Cursor = dbInventario.obtenerArticulo(idArticulo)

            val descripcionIndex = idArticuloCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)
            val stockIndex = idArticuloCursor.getColumnIndex(DBInventario.COLUMN_STOCK_REAL)

            val descripcion = idArticuloCursor.getString(descripcionIndex)
            val stock = idArticuloCursor.getString(stockIndex)

            // Se establece el cursor para buscar en la tabla de Partidas:
            val partidaCursor: Cursor = dbInventario.obtenerPartidaPorArticulo(idArticulo)

            val partidaIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_PARTIDA)
            val fechaIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_FECHA_CADUCIDAD)
            val numeroSerieIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_NUMERO_SERIE)

            val partida = partidaCursor.getString(partidaIndex)
            val fecha = partidaCursor.getString(fechaIndex)
            val numeroSerie = partidaCursor.getString(numeroSerieIndex)

            // Para pintar en pantalla el código de barras:
            if(!codigoBarras.isNullOrEmpty()){
                binding.tvCodigo2.setText(codigoBarras)
            }else{
                binding.tvCodigo2.setText("")
            }


            // Para pintar en pantalla la descripción:
            if(!descripcion.isNullOrEmpty()){
                binding.tvDescripcion2.setText(codigoBarras)
            }else{
                binding.tvDescripcion2.setText("")
            }



            // Para pintar en pantalla el Id de Artículo:
            if(!descripcion.isNullOrEmpty()){
                binding.tvIdArticulo2.setText(idArticulo)
            }else{
                binding.tvIdArticulo2.setText("")
            }



            // Para pintar en pantalla el Id de combinación:
            if(!descripcion.isNullOrEmpty()){
                binding.tvIdCombinacion2.setText(idCombinacion)
            }else{
                binding.tvIdCombinacion2.setText("")
            }


            // Para pintar en pantalla el la partida:
            if(!descripcion.isNullOrEmpty()){
                binding.tvPartida2.setText(partida)
            }else{
                binding.tvPartida2.setText("")
            }


            // Para pintar en pantalla el la fecha de caducidad:
            if(!descripcion.isNullOrEmpty()){
                binding.tvFecha2.setText(fecha)
            }else{
                binding.tvFecha2.setText("")
            }


            // Para pintar en pantalla el número de serie:
            if(!descripcion.isNullOrEmpty()){
                binding.tvNumero2.setText(numeroSerie)
            }else{
                binding.tvNumero2.setText("")
            }


            // Para pintar en pantalla el número de unidades en Stock:
            if(!descripcion.isNullOrEmpty()){
                binding.tvUnidades2.setText(stock)
            }else{
                binding.tvUnidades2.setText("")
            }

        } else {
            // Si no se encuentra el código de barras, mostramos un mensaje
            Toast.makeText(this, "Código de barras no encontrado en la base de datos", Toast.LENGTH_LONG).show()
        }
    }

    // Inicializar la base de datos
        private fun inicializarDB(){

        dbInventario = DBInventario(this)
    }

    // Actualizar número de Stock:
    private fun actualizarStock(){

        val idArticulo:String = binding.tvIdArticulo2.text.toString()


        // Obtener el valor del TextView como String
        val unidadesStockString = binding.tvUnidades2.text.toString()

// Convertirlo a Int de forma segura (en caso de que no sea un número válido)
        val unidadesStock: Int = unidadesStockString.toInt()

// Si es válido, puedes usarlo. Si no, unidadesStock será null, y puedes manejar ese caso.
        if (unidadesStock != null) {
            // Código para actualizar el stock
            println("El stock es: $unidadesStock")
        } else {
            // Manejar el caso en que la conversión falla
            println("Error: el valor ingresado no es un número válido.")
        }

        if(!idArticulo.isNullOrEmpty()){

            dbInventario.actualizarStock(idArticulo, unidadesStock)
        }else{

            Toast.makeText(this, "No se puede actualizar el stock de un artículo no registrado",
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