package com.example.indotinventario.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.indotinventario.utilidades.Constantes
import com.example.indotinventario.R
import com.example.indotinventario.logica.UploadWriteJsonFile
import com.example.indotinventario.databinding.ActivityBuscarCodigoBarrasBinding
import com.example.indotinventario.logica.DBInventario
import com.example.indotinventario.logica.DBUsuarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList
import www.sanju.motiontoast.MotionToast

class BuscarCodigoBarrasActivity : AppCompatActivity() {

    // Se crea el binding para la vista:
    private lateinit var binding:ActivityBuscarCodigoBarrasBinding
    // Variable para acceder a la DB de Inventario:
    private lateinit var dbInventario: DBInventario

    // Variable para acceder a la DB de Usuarios:
    private lateinit var dbUsuarios: DBUsuarios

    // ArrayLists para almacenar los valores asociados a las partidas:
    private var arrayPartidas:ArrayList<String?> = ArrayList()
    private var arrayFechas:ArrayList<String?> = ArrayList()
    private var arrayNumerosSerie:ArrayList<String?> = ArrayList()

    private lateinit var adapterPartidas:ArrayAdapter<Any>
    private lateinit var adapterNumerosSerie:ArrayAdapter<Any>

    // Constantes y variables para permisos de cámara:
    private val CODIGO_PERMISOS_CAMARA = 1

    private var permisoCamaraConcedido = false
    private var permisoSolicitadoDesdeBoton = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // Implementación de View Binding:
        binding = ActivityBuscarCodigoBarrasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarVista()
        inicializarDB()
        verificarYPedirPermisosDeCamara()

        comprobarBundleDescripcion()
    }

    private fun comprobarBundleDescripcion() {

        val bundle = intent.extras
        val codigoBarras = bundle?.getString("codigoBarras")
        val idArticulo = bundle?.getString("idArticulo")
        val idCombinacion = bundle?.getString("idCombinacion")

        if (bundle != null) {
            if(bundle.isEmpty ){
                Log.i("Bundle", "Bundle vacío")
            }else{
                binding.etCodigo.setText(codigoBarras)
                binding.tvIdArticulo2.text = idArticulo
                binding.tvIdCombinacion2.text = idCombinacion

                buscarArticuloPorIdArticulo(idArticulo)
                if (idArticulo != null) {
                    buscarPartidaPorIdArticulo(idArticulo)
                }
            }
        }else{
            Log.i("Bundle", "Bundle nulo")
        }
    }

    private fun cargarVista() {

        binding.etCodigo.filters = arrayOf(InputFilter.AllCaps())
        binding.buttonEscanear.setOnClickListener {

            if (!permisoCamaraConcedido) {

                MotionToast.createToast(this,
                    "ERROR CÁMARA",
                    "Permiso de cámara no concedido",
                    MotionToast.TOAST_ERROR,
                    MotionToast.GRAVITY_CENTER,
                    MotionToast.SHORT_DURATION,
                    null)

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

        binding.buttonBuscar.setOnClickListener {
            buscarporCodigoBarras()
        }

        binding.buttonGuardar.setOnClickListener {
            insertarItemInventario()
            limpiarCampos()
        }

        binding.buttonInfo.setOnClickListener{
            mostrarDialogInformacion()
        }
    }

    private fun buscarporCodigoBarras() {

        val codigoArticulo = binding.etCodigo.text.toString()
        val idArticulo = buscarArticuloPorCodigoBarras(codigoArticulo)
        buscarArticuloPorIdArticulo(idArticulo)
        buscarPartidaPorIdArticulo(idArticulo)
    }

    private fun disminuirUnidades() {

        val currentValue = binding.etUnidades.text.toString()
        val unidades = if (currentValue.isNotEmpty()) {
            currentValue.toIntOrNull() ?: 0
        } else {
            0
        }

        val newValue = if (unidades > 0) unidades - 1 else 0
        binding.etUnidades.setText(newValue.toString())
    }

    private fun incrementarUnidades() {
        val currentValue = binding.etUnidades.text.toString()
        val unidades = if (currentValue.isNotEmpty()) {
            currentValue.toIntOrNull() ?: 0 // Si no es un número válido, usar 0
        } else {
            0
        }

        val newValue = unidades + 1
        binding.etUnidades.setText(newValue.toString())
    }

    //ESCÁNER:
    // Declara el launcher para iniciar la actividad y manejar el resultado
    private val escanearLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Verifica si el resultado fue exitoso y que el requestCode coincide
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra("codigo")?.let { codigoBarras ->
                // Aquí se maneja el código de barras como antes
                val codigoArticulo: String = codigoBarras
                binding.etCodigo.setText(codigoArticulo)
                buscarporCodigoBarras()
            }
        }
    }

    private fun escanear() {
        val intent = Intent(this, EscanearActivity::class.java)
        escanearLauncher.launch(intent)
    }

    private fun buscarArticuloPorIdArticulo(idArticulo: String?) {

        try {
            val articuloCursor: Cursor = dbInventario.obtenerArticulo(idArticulo!!)

            if (articuloCursor.moveToFirst() || articuloCursor.moveToNext()) {

                val idCombinacionIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                val descripcionIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)

                val idCombinacion = articuloCursor.getString(idCombinacionIndex)
                val descripcion = articuloCursor.getString(descripcionIndex)

                articuloCursor.close() //Se cierra el cursor

                binding.tvIdCombinacion2.setText(idCombinacion)
                binding.tvDescripcion2.setText(descripcion)

            }else{

                Log.e("ERROR", "No se obtuvo ningún resultado")
            }
        }catch(e:Exception){

            Log.e("ERROR", "No se obtuvo ningún resultado")
        }
    }

    private fun buscarPartidaPorIdArticulo(idArticulo: String) {

        try{
            val partidaCursor: Cursor = dbInventario.obtenerPartidaPorIdArticulo(idArticulo)
            var partidaInicial = ""

            if(partidaCursor.moveToFirst()) {

                arrayPartidas.clear()
                arrayFechas.clear()
                arrayNumerosSerie.clear()

                do{
                    val partidaIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_PARTIDA)
                    val fechaCaducidadIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_FECHA_CADUCIDAD)
                    val numeroSerieIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_NUMERO_SERIE)

                    val partida = partidaCursor.getString(partidaIndex)
                    val fechaCaducidad = partidaCursor.getString(fechaCaducidadIndex)
                    val numeroSerie = partidaCursor.getString(numeroSerieIndex)

                    partidaInicial = partida

                    arrayPartidas.add(partida)
                    arrayFechas.add(fechaCaducidad)
                    arrayNumerosSerie.add(numeroSerie)

                }while(partidaCursor.moveToNext())

                partidaCursor.close() //Se cierra el cursor
            }else{
                Log.i("TAG Partida", "Elemento no encontrado")
            }

            //Si la partida está vacía (se transforma desde el nulo del fichero descargado), es un número de serie
            if(partidaInicial == Constantes.CADENA_VACIA){

                arrayPartidas.clear()
                arrayFechas.clear()

                val items = listOf(*arrayNumerosSerie.toTypedArray())

                adapterNumerosSerie = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

                adapterNumerosSerie.setDropDownViewResource(android.R.layout.simple_spinner_item)

                binding.spinnerNumeroSerie.adapter = adapterNumerosSerie

                binding.spinnerNumeroSerie.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {

                        binding.etUnidades.setText("1")
                        binding.etUnidades.isFocusable = false
                        binding.etUnidades.isClickable = false

                        binding.buttonIncrementar.isFocusable = false
                        binding.buttonIncrementar.isClickable = false

                        binding.buttonDisminuir.isFocusable = false
                        binding.buttonDisminuir.isClickable = false
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
                //Si el número de serie está vacío(se transforma desde el nulo del fichero descargado), es una partida,
                // que puede tener fecha de caducidad
            }else{
                arrayNumerosSerie.clear()

                val items = listOf(*arrayPartidas.toTypedArray())

                adapterPartidas = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
                adapterPartidas.setDropDownViewResource(android.R.layout.simple_spinner_item)
                binding.spinnerPartida.adapter = adapterPartidas
                binding.spinnerPartida.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {

                        if (position >= 0 && position < arrayPartidas.size){

                            binding.tvFecha2.setText(arrayFechas[position])

                        }else {

                            MotionToast.createToast(this@BuscarCodigoBarrasActivity,
                                "ATENCIÓN",
                                "No hay ningún elemento disponible",
                                MotionToast.TOAST_WARNING,
                                MotionToast.GRAVITY_CENTER,
                                MotionToast.SHORT_DURATION,
                                null)
                        }
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
            }
        }catch(e:Exception){
            MotionToast.createToast(this@BuscarCodigoBarrasActivity,
                "ATENCIÓN",
                "No hay ningún elemento disponible",
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_CENTER,
                MotionToast.SHORT_DURATION,
                null)
        }
    }

    private fun buscarArticuloPorCodigoBarras(codigoBarras: String):String {

        var idArticulo = ""
        try{
            val cursor: Cursor = dbInventario.obtenerCodigoBarras(codigoBarras)

            if (cursor.moveToFirst() || cursor.moveToNext()) {

                val idArticuloIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                idArticulo = cursor.getString(idArticuloIndex)
                binding.tvIdArticulo2.text = idArticulo

                cursor.close() //Se cierra el cursor

            } else {
                MotionToast.createToast(this,
                    "ERROR",
                    "Código de barras no encontrado",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_CENTER,
                    MotionToast.SHORT_DURATION,
                    null)

            }
        }catch(e:Exception){
            Log.e("Error", "Código de barras no encontrado")
        }
        return idArticulo
    }

    // Inicializar las bases de datos
    private fun inicializarDB(){

        dbInventario = DBInventario.getInstance(this)
        dbUsuarios = DBUsuarios.getInstance(this)
    }

    private fun limpiarCampos() { //Se reinicia la activity para poder limpiar todos los campos de la vista

        try{
            val intent = Intent(this@BuscarCodigoBarrasActivity, this@BuscarCodigoBarrasActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
            finish()
        }catch(e:Exception){
            Log.e("Error Activity", "Error al reiniciar los campos")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODIGO_PERMISOS_CAMARA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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

            permisoCamaraConcedido = true
        } else {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CODIGO_PERMISOS_CAMARA)
        }
    }

    private fun permisoDeCamaraDenegado() {

        MotionToast.createToast(this,
            "ERROR CÁMARA",
            "Permiso de la cámara denegado",
            MotionToast.TOAST_WARNING,
            MotionToast.GRAVITY_CENTER,
            MotionToast.SHORT_DURATION,
            null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Se sobreescribe el menú de los 3 puntitos

        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //Se asignan métodos al menú
        return when (item.itemId) {

            R.id.idInicio -> {

                dbInventario.close()
                finish()
                true
            }

            R.id.idSalir -> {

                showAlertDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun insertarItemInventario() {

        try{
            val codigoBarras = binding.etCodigo.text?.toString() ?: Constantes.CADENA_VACIA
            val descripcion = binding.tvDescripcion2.text?.toString() ?: Constantes.CADENA_VACIA
            val idArticulo = binding.tvIdArticulo2.text?.toString() ?: Constantes.CADENA_VACIA
            val idCombinacion = binding.tvIdCombinacion2.text?.toString() ?: Constantes.CADENA_VACIA
            val fechaCaducidad = binding.tvFecha2.text?.toString() ?: Constantes.CADENA_VACIA

            val unidadesContadas: Double = binding.etUnidades.text.toString().toDouble()
            val partida = binding.spinnerPartida.selectedItem?.toString() ?: Constantes.CADENA_VACIA
            val numeroSerie = binding.spinnerNumeroSerie.selectedItem?.toString() ?: Constantes.CADENA_VACIA

            if(descripcion.isEmpty() && idArticulo.isEmpty()){

                MotionToast.createToast(this,
                    "INFORMACIÓN INCOMPLETA",
                    "Algunos de los campos no pueden estar vacíos",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_CENTER,
                    MotionToast.SHORT_DURATION,
                    null)

            }else if(unidadesContadas < 0){

                MotionToast.createToast(this,
                    "INFORMACIÓN INCOMPLETA",
                    "Algunos de los campos no pueden estar vacíos",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_CENTER,
                    MotionToast.SHORT_DURATION,
                    null)

            }else{

                dbInventario.insertarItemInventario(codigoBarras, descripcion, idArticulo,
                    idCombinacion, partida, fechaCaducidad,
                    numeroSerie, unidadesContadas)

                dbInventario.eliminarArticulo(idArticulo) //Se elimina el artículo buscado una vez contadas las unidades
            }
        }catch(e:Exception){
            MotionToast.createToast(this,
                "INFORMACIÓN INCOMPLETA",
                "Algunos de los campos no pueden estar vacíos",
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_CENTER,
                MotionToast.SHORT_DURATION,
                null)
        }
    }

    private fun showAlertDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar salida")
            .setMessage("¿Estás seguro de que quieres guardar los cambios del " +
                    "inventario en la nube y salir de la app?")

            .setPositiveButton("Sí") { dialog, which ->

                dbInventario.close()

                // Se llama a corrutina para salvar el inventario de la DB a un fichero Json en almacenamiento externo:
                lifecycleScope.launch(Dispatchers.IO){

                    if(dbInventario.obtenerTodosItemInventario().count <= 0){

                        withContext(Dispatchers.Main){
                            MotionToast.createToast(this@BuscarCodigoBarrasActivity,
                                "SIN UNIDADES GUARDADAS",
                                "No se han guardado unidades de ningún artículo",
                                MotionToast.TOAST_WARNING,
                                MotionToast.GRAVITY_CENTER,
                                MotionToast.SHORT_DURATION,
                                null)
                        }
                    }else{

                        async{UploadWriteJsonFile.uploadJsonInventario(this@BuscarCodigoBarrasActivity, dbInventario, dbUsuarios)}.await()

                        if(UploadWriteJsonFile.isUploadOK()){

                            finishAffinity() // Finaliza la app.
                        }else {
                            withContext(Dispatchers.Main) {
                                MotionToast.createToast(
                                    this@BuscarCodigoBarrasActivity,
                                    "ERROR DE SUBIDA DE FICHERO",
                                    "Revisar conexión a Internet o consultar con el administrador de la Api",
                                    MotionToast.TOAST_ERROR,
                                    MotionToast.GRAVITY_CENTER,
                                    MotionToast.SHORT_DURATION,
                                    null
                                )
                            }
                        }
                    }
                }
            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
    }

    private fun mostrarDialogInformacion() {

        val dialogView = layoutInflater.inflate(R.layout.popup_information, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.show()
    }
}