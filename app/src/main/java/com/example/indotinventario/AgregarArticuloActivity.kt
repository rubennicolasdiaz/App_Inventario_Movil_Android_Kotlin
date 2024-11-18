package com.example.indotinventario

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.indotinventario.databinding.ActivityAgregarArticuloBinding
import java.util.Date

class AgregarArticuloActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarArticuloBinding

    // Variable para acceder a la DB:
    private lateinit var dbInventario: DBInventario

    // Constantes y variables para permisos de cámara:
    val CODIGO_INTENT_ESCANEAR = 3
    val CODIGO_PERMISOS_CAMARA = 1

    private var permisoCamaraConcedido = false
    private var permisoSolicitadoDesdeBoton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityAgregarArticuloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se verifican los permisos de cámara
        verificarYPedirPermisosDeCamara()

        cargarVista()

        // Se inicializa base de datos:
        inicializarDB()
    }






        // Inicializar la base de datos
        private fun inicializarDB(){
            dbInventario = DBInventario(this)
        }




    private fun obtenerFechaActualEnMilisegundos(): Long {
        val fechaActual = Date()  // Obtiene la fecha actual
        return fechaActual.time    // Devuelve la fecha en milisegundos desde el 1 de enero de 1970
    }

    private fun cargarVista() {


        binding.etDate.setOnClickListener {



            // Abrir el DatePicker cuando el usuario hace clic en el EditText
            showDatePickerDialog()
            // Esconde el teclado si aparece
            esconderTeclado()

        }




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
            insertarArticulo()
        }
    }

    private fun showDatePickerDialog() {

        val datePicker = DatePickerFragment{day, month, year -> onDateSelected(day, month, year)}
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun onDateSelected(day:Int, month:Int,year:Int){

        var mes = month + 1
        binding.etDate.setText("$day-$mes -$year")
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

    private fun limpiarCampos(){

        binding.tvCodigo2.setText("")
        binding.etDescripcion.setText("")
        binding.etIdArticulo.setText("")
        binding.etIdCombinacion.setText("")
        binding.etPartida.setText("")
        binding.etNumero.setText("")
        binding.tvUnidades2.setText("")
        binding.etDate.setText("")
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
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                CODIGO_PERMISOS_CAMARA
            )
        }
    }

    private fun permisoDeCamaraDenegado() {

        Toast.makeText(this, "Permiso de la cámara denegado", Toast.LENGTH_SHORT).show()
    }

    // Escáner

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


                binding.tvCodigo2.setText(codigoArticulo) // Código de barras



            }
        }
    }

    private fun insertarArticulo(){

        try {

            // Extraer el número entero de tvUnidades2 y almacenarlo en una variable de tipo Int
            val unidadesTexto = binding.tvUnidades2.text.toString()  // Obtiene el texto
            val unidades: Int = unidadesTexto.toIntOrNull() ?: 0  // Convierte a Int, o 0 si no es un número válido


            val unidadesDouble: Double = unidades.toDouble()
            var codigoArticulo = binding.tvCodigo2.text.toString() // Se almacena el código de barras en variable



            if(!codigoArticulo.isNullOrEmpty()){

                // Inserción de artículos
                dbInventario.insertarArticulo(binding.etIdArticulo.text.toString(), binding.etDescripcion.text.toString(), unidadesDouble, binding.etIdCombinacion.text.toString())


                // Inserción de códigos de barras
                dbInventario.insertarCodigoBarras(codigoArticulo, binding.etIdArticulo.text.toString(), binding.etIdCombinacion.text.toString())

                // Inserción de partida
                dbInventario.insertarPartida(binding.etIdArticulo.text.toString(), binding.etPartida.text.toString(),
                    binding.etDate.text.toString(), binding.etNumero.text.toString())

                limpiarCampos()

                Toast.makeText(this, "Artículo añadido a la base de datos",
                    Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(this, "No se puede añadir un nuevo artículo sin código de barras",
                    Toast.LENGTH_SHORT).show()
            }

        }catch(e:Exception){
            Toast.makeText(this, "Error: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
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

    // Si el teclado está visible, se esconde
    private fun esconderTeclado() {

        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.viewRoot.windowToken, 0)
    }
}
