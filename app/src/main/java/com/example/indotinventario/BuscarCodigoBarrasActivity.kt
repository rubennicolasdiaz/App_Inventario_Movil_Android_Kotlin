package com.example.indotinventario

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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

    private lateinit var adapter:ArrayAdapter<Any>

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

        // Se rellena el Array de Artículos al inicio de la Activity
        rellenarArrayArticulos()

        //Comprobar el Bundle de la activity de Buscar por Descripción
        comprobarBundleDescripcion()
    }

    private fun comprobarBundleDescripcion() {

            val bundle = intent.extras
            val codigoBarras = bundle?.getString("codigoBarras")
            val idArticulo = bundle?.getString("idArticulo")


            if (!codigoBarras.isNullOrEmpty()) {
                binding.etCodigo.setText(codigoBarras)
                binding.tvIdArticulo2.text = idArticulo

                buscarArticuloPorCodigoBarras(codigoBarras)
            }else{

                if(!idArticulo.isNullOrEmpty()) {
                    binding.tvIdArticulo2.text = idArticulo
                    buscarArticuloPorIdArticulo(idArticulo)
                    buscarPartidaPorIdArticulo(idArticulo)
                }
            }
    }

    private fun cargarVista() {

        binding.buttonEscanear.setOnClickListener {

            if (!permisoCamaraConcedido) {
                Toast.makeText(this, "Permiso de cámara no concedido", Toast.LENGTH_SHORT).show()
                permisoSolicitadoDesdeBoton = true
                verificarYPedirPermisosDeCamara()
                return@setOnClickListener
            }
            // Limpiar campos antes de escanear para que se borre el Spinner de Partidas
            limpiarCampos()
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
        buscarArticuloPorCodigoBarras(codigoArticulo)
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
                buscarArticuloPorCodigoBarras(codigoArticulo)
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

                Toast.makeText(this, "No se obtuvo ningún resultado", Toast.LENGTH_SHORT).show()
            }

        }catch(e:Exception){

            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun buscarPartidaPorIdArticulo(idArticulo: String?) {

        try{

            val partidaCursor: Cursor = dbInventario.obtenerPartidaPorIdArticulo(idArticulo!!)

            if(partidaCursor.moveToFirst() || partidaCursor.moveToNext()) {

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

                // Cerramos el cursor
                partidaCursor.close()


                val items = listOf(*arrayPartidas.toTypedArray())
                adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

                binding.spinnerPartida.adapter = adapter

                binding.spinnerPartida.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{


                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {

                        if (position >= 0 && position < arrayPartidas.size){

                            binding.tvFecha2.setText(arrayFechas[position])

                            if(arrayNumerosSerie[position].equals("null")){

                                binding.tvNumero2.setText("")
                            }else{
                                binding.tvNumero2.setText(arrayNumerosSerie[position])
                            }
                        }else {
                            Toast.makeText(this@BuscarCodigoBarrasActivity, "No hay ningún elemento disponible",
                                Toast.LENGTH_SHORT).show()
                        }

                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }
                }
            }

        }catch(e:Exception){
            Toast.makeText(this, e.message,Toast.LENGTH_SHORT).show()
        }

    }

    private fun buscarArticuloPorCodigoBarras(codigoBarras: String) {

        try{
            // Obtener el cursor con los datos del código de barras
            val cursor: Cursor = dbInventario.obtenerCodigoBarras(codigoBarras)

            // Verificamos si el cursor tiene resultados
            if (cursor.moveToFirst() || cursor.moveToNext()) {
                // Obtener los valores del cursor para el código de barras
                val idArticuloIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                val idCombinacionIndex = cursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)


                val idArticulo = cursor.getString(idArticuloIndex)
                val idCombinacion = cursor.getString(idCombinacionIndex)

                // Cerramos el cursor de código de barras
                cursor.close()

                binding.tvIdArticulo2.setText(idArticulo) // ID del artículo
                binding.tvIdCombinacion2.setText(idCombinacion) // Combinación (suele venir vacío)


                // Ahora obtenemos los detalles del artículo usando el idArticulo
                val articuloCursor: Cursor = dbInventario.obtenerArticulo(idArticulo)

                if(articuloCursor.moveToFirst() || articuloCursor.moveToNext()){

                    // Verificamos si se encuentra el artículo

                    val descripcionIndex = articuloCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)

                    // Obtener los detalles del artículo
                    val descripcion = articuloCursor.getString(descripcionIndex)

                    // Cerramos el cursor de artículo
                    articuloCursor.close()

                    // Asignamos los valores a los EditText en la interfaz de usuario
                    binding.tvDescripcion2.setText(descripcion) // Descripción del artículo

                    // Ahora obtenemos los detalles de la partida usando el idArticulo
                    val partidaCursor: Cursor = dbInventario.obtenerPartidaPorIdArticulo(idArticulo)


                    // Recorremos todas las filas de partida asociadas al idArticulo
                    if(partidaCursor.moveToFirst() || partidaCursor.moveToNext()) {

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

                        // Cerramos el cursor
                        partidaCursor.close()


                        val items = listOf(*arrayPartidas.toTypedArray())
                        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)



                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

                        binding.spinnerPartida.adapter = adapter

                        binding.spinnerPartida.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{


                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {

                                if (position >= 0 && position < arrayPartidas.size){

                                    binding.tvFecha2.setText(arrayFechas[position])

                                    if(arrayNumerosSerie[position].equals("null")){

                                        binding.tvNumero2.setText("")
                                    }else{
                                        binding.tvNumero2.setText(arrayNumerosSerie[position])
                                    }
                                }else {
                                    Toast.makeText(this@BuscarCodigoBarrasActivity, "No hay ningún elemento disponible",
                                        Toast.LENGTH_SHORT).show()
                                }

                            }
                            override fun onNothingSelected(p0: AdapterView<*>?) {

                            }
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

        dbInventario = DBInventario.getInstance(this)
    }

    private fun limpiarCampos() {

        try{
            //Vaciamos los arrays para volver a dejarlos a 0 para la siguiente búsqueda
            arrayPartidas.clear()
            arrayFechas.clear()
            arrayNumerosSerie.clear()

            //Se establece un adaptador vacío para limpiar las filas del spinner
            val emptyAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOf(""))
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            binding.spinnerPartida.adapter = emptyAdapter

            //Se setean los campos de la vista con cadenas vacías

            binding.tvFecha2.setText("")
            binding.tvNumero2.setText("")
            binding.etCodigo.setText("")
            binding.tvDescripcion2.setText("")
            binding.tvIdArticulo2.setText("")
            binding.tvIdCombinacion2.setText("")
            binding.etUnidades.setText("")

        }catch(e:Exception){
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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

                showAlertDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun rellenarArrayArticulos() {
        try {
            val cursorArticulos: Cursor = dbInventario.obtenerTodosArticulos()

            // Verificar si el cursor tiene registros
            if (cursorArticulos.moveToFirst()) {

                do {
                    // Obtener los índices de las columnas
                    val idArticuloIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                    val idCombinacionIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                    val descripcionIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)
                                        // Obtener los valores del artículo
                    val idArticulo = cursorArticulos.getString(idArticuloIndex)
                    val idCombinacion = cursorArticulos.getString(idCombinacionIndex)
                    val descripcion = cursorArticulos.getString(descripcionIndex)

                    // Crear la instancia de la clase Articulo
                    val articulo = Articulo(idArticulo, idCombinacion, descripcion)

                    // Agregar la instancia de Articulo a la lista
                    arrayArticulos.add(articulo)

                } while (cursorArticulos.moveToNext()) // Avanzar al siguiente registro

            } else {
                Toast.makeText(this, "No hay artículos en la base de datos", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun insertarItemInventario() {

        try{
            val codigoBarras = binding.etCodigo.text?.toString() ?: ""
            val descripcion = binding.tvDescripcion2.text?.toString() ?: ""
            val idArticulo = binding.tvIdArticulo2.text?.toString() ?: " "
            val idCombinacion = binding.tvIdCombinacion2.text?.toString() ?: " "
            val fechaCaducidad = binding.tvFecha2.text?.toString() ?: ""
            val numeroSerie = binding.tvNumero2.text?.toString() ?: " "
            val unidadesContadas: Double = binding.etUnidades.text.toString().toDouble()
            val partida = binding.spinnerPartida.selectedItem?.toString() ?: " "

            if(descripcion.isEmpty() && idArticulo.isEmpty()){

                Toast.makeText(this, "Algunos de los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            }else if(unidadesContadas < 0){

                Toast.makeText(this, "Algunos de los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            }else{

                dbInventario.insertarItemInventario(codigoBarras, descripcion, idArticulo,
                    idCombinacion, partida, fechaCaducidad,
                    numeroSerie, unidadesContadas)
            }
        }catch(e:Exception){
            Toast.makeText(this, "Algunos de los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
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


