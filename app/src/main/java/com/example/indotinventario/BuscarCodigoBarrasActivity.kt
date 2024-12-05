package com.example.indotinventario

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
import com.example.indotinventario.Pruebas.Articulo
import com.example.indotinventario.Pruebas.SaveJsonFile
import com.example.indotinventario.databinding.ActivityBuscarCodigoBarrasBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import www.sanju.motiontoast.MotionToast

class BuscarCodigoBarrasActivity : AppCompatActivity() {

    // Se crea el binding para la vista:
    private lateinit var binding:ActivityBuscarCodigoBarrasBinding
    // Variable para acceder a la DB:
    private lateinit var dbInventario: DBInventario

    // ArrayList para poder buscar por descripción de artículo:
    private var arrayArticulos: ArrayList<Articulo> = ArrayList()

    // ArrayLists para almacenar los valores asociados a las partidas:
    private var arrayPartidas:ArrayList<String> = ArrayList()
    private var arrayFechas:ArrayList<String> = ArrayList()
    private var arrayNumerosSerie:ArrayList<String> = ArrayList()

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
                buscarPartidaPorIdArticulo(idArticulo)
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
    }

    private fun buscarporCodigoBarras() {

        val codigoArticulo = binding.etCodigo.text.toString()
        val idArticulo = buscarArticuloPorCodigoBarras(codigoArticulo)
        buscarArticuloPorIdArticulo(idArticulo)
        buscarPartidaPorIdArticulo(idArticulo)
    }

    private fun disminuirUnidades() {

        val currentValue = binding.etUnidades.text.toString()

        // Verificar si el texto es un número válido
        val unidades = if (currentValue.isNotEmpty()) {
            currentValue.toIntOrNull() ?: 0 // Si no es un número válido, usar 0
        } else {
            0
        }

        // Disminuir la cantidad, asegurándonos de que no sea menor que 0
        val newValue = if (unidades > 0) unidades - 1 else 0

        // Actualizar el EditText con el nuevo valor
        binding.etUnidades.setText(newValue.toString())
    }

    private fun incrementarUnidades() {
        val currentValue = binding.etUnidades.text.toString()

        // Verificar si el texto es un número válido
        val unidades = if (currentValue.isNotEmpty()) {
            currentValue.toIntOrNull() ?: 0 // Si no es un número válido, usar 0
        } else {
            0
        }

        // Incrementar la cantidad
        val newValue = unidades + 1

        // Actualizar el EditText con el nuevo valor
        binding.etUnidades.setText(newValue.toString())
    }

    //ESCÁNER:
    // Declara el launcher para iniciar la actividad y manejar el resultado
    private val escanearLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Verifica si el resultado fue exitoso y que el requestCode coincide
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra("codigo")?.let { codigoBarras ->
                // Aquí manejas el código de barras como antes
                val codigoArticulo: String = codigoBarras
                binding.etCodigo.setText(codigoArticulo)
                buscarporCodigoBarras()
            }
        }
    }

    // Método para iniciar la actividad de escaneo
    private fun escanear() {
        val intent = Intent(this, EscanearActivity::class.java)
        escanearLauncher.launch(intent)
    }

    private fun buscarArticuloPorIdArticulo(idArticulo: String?) {

        try {
            // Ahora obtenemos los detalles del artículo usando el idArticulo

            val articuloCursor: Cursor = dbInventario.obtenerArticulo(idArticulo!!)

            if (articuloCursor.moveToFirst() || articuloCursor.moveToNext()) {

                val idCombinacionIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                val descripcionIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)

                val idCombinacion = articuloCursor.getString(idCombinacionIndex)
                val descripcion = articuloCursor.getString(descripcionIndex)

                // Cerramos el cursor de artículo
                articuloCursor.close()

                binding.tvIdCombinacion2.setText(idCombinacion)
                binding.tvDescripcion2.setText(descripcion)

            }else{

                Log.e("ERROR", "No se obtuvo ningún resultado")
            }

        }catch(e:Exception){

            Log.e("ERROR", "No se obtuvo ningún resultado")
        }
    }

    private fun buscarPartidaPorIdArticulo(idArticulo: String?) {

        try{

            val partidaCursor: Cursor = dbInventario.obtenerPartidaPorIdArticulo(idArticulo!!)
            var partidaInicial = ""

            if(partidaCursor.moveToFirst()) {

                arrayPartidas.clear()
                arrayFechas.clear()
                arrayNumerosSerie.clear()

                do{
                    val partidaIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_PARTIDA)
                    val fechaCaducidadIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_FECHA_CADUCIDAD)
                    val numeroSerieIndex = partidaCursor.getColumnIndex(DBInventario.COLUMN_NUMERO_SERIE)

                    // Obtener los detalles de la partida:

                    val partida = partidaCursor.getString(partidaIndex)
                    val fechaCaducidad = partidaCursor.getString(fechaCaducidadIndex)
                    val numeroSerie = partidaCursor.getString(numeroSerieIndex)

                    arrayPartidas.add(partida)
                    arrayFechas.add(fechaCaducidad)
                    arrayNumerosSerie.add(numeroSerie)

                }while(partidaCursor.moveToNext())

                partidaInicial = arrayPartidas.last()
                partidaCursor.close()


            }else{
                Log.i("TAG Partida", "Elemento no encontrado")
            }

            if(partidaInicial == "null"){

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
            // Obtener el cursor con los datos del código de barras
            val cursor: Cursor = dbInventario.obtenerCodigoBarras(codigoBarras)

            // Verificamos si el cursor tiene resultados
            if (cursor.moveToFirst() || cursor.moveToNext()) {
                // Obtener los valores del cursor para el código de barras
                val idArticuloIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)

                idArticulo = cursor.getString(idArticuloIndex)

                binding.tvIdArticulo2.text = idArticulo

                cursor.close()

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

    // Inicializar la base de datos
    private fun inicializarDB(){

        dbInventario = DBInventario.getInstance(this)
    }

    private fun limpiarCampos() {

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

                showAlertDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun insertarItemInventario() {

        try{
            val codigoBarras = binding.etCodigo.text?.toString() ?: ""
            val descripcion = binding.tvDescripcion2.text?.toString() ?: ""
            val idArticulo = binding.tvIdArticulo2.text?.toString() ?: " "
            val idCombinacion = binding.tvIdCombinacion2.text?.toString() ?: " "
            val fechaCaducidad = binding.tvFecha2.text?.toString() ?: ""

            val unidadesContadas: Double = binding.etUnidades.text.toString().toDouble()
            val partida = binding.spinnerPartida.selectedItem?.toString() ?: " "
            val numeroSerie = binding.spinnerNumeroSerie.selectedItem?.toString() ?: " "

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
                    "inventario y salir de la app?")

            .setPositiveButton("Sí") { dialog, which ->

                dbInventario.close()

                // Se llama a corrutina para salvar el inventario de la DB a un fichero Json en almacenamiento externo:
                lifecycleScope.launch(Dispatchers.IO){

                    async{ SaveJsonFile.saveJsonInventario(this@BuscarCodigoBarrasActivity, dbInventario)}.await()
                    finishAffinity() // Finaliza la app.
                }

            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
    }
}