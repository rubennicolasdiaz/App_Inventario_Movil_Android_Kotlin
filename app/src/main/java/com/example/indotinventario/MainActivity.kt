package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityMainBinding
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.InputStream
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    // Instancia de la clase DBInventario
    private lateinit var dbInventario: DBInventario
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // Dormimos el hilo principal 2 segundos para que se vea el screen y se ejecute la corrutina
        // para leer los Json y pasar los datos a SQLite:
        Thread.sleep(2000)
        setTheme(R.style.AppTheme)


        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se cargan los componentes gráficos de la vista:
        cargarVista()

        // Se inicializa la DB:
        inicializarDB()

        lifecycleScope.launch(Dispatchers.IO){
            // Se cargan los ficheros Json de la carpeta Assets y se pasan a la DB:
            loadJsonArticulos()

            loadJsonCodigosBarras()

            loadJsonPartidas()
        }
    }

    // Se inicializan los elementos de la vista:
    private fun cargarVista() {

        binding.etUsuario
        binding.etPassword

        binding.buttonLogin.setOnClickListener {
            comprobarLogin()
        }

        binding.buttonSalir.setOnClickListener {
            cerrarAplicacion()
        }
    }

    // Login:

    private fun comprobarLogin() {

        pasarAMenuActivity()

        // De momento para las pruebas dejamos el login sin funcionalidad

        /*
        val con = ConsultaWS(etUsuario.text.toString().trim(), etContraseña.text.toString().trim())

        if (con.verCosas(etUsuario, etContraseña)) {

            pasarAMenuActivity()

        } else {
            Toast.makeText(this, R.string.error_login, Toast.LENGTH_SHORT).show()
        }
        */
    }

    private fun inicializarDB() {

        dbInventario = DBInventario.getInstance(this)

        // Obtener la base de datos en modo escritura
        val db = dbInventario.writableDatabase

        // Eliminar las tablas existentes
        db.execSQL("DROP TABLE IF EXISTS Articulos")
        db.execSQL("DROP TABLE IF EXISTS CodigosBarras")
        db.execSQL("DROP TABLE IF EXISTS Partidas")

        // Volver a crear las tablas
        dbInventario.onCreate(db)
    }

    private fun pasarAMenuActivity() {

        startActivity(Intent(this, MenuActivity::class.java))
        dbInventario.close()
        finish()
    }

    private fun cerrarAplicacion() {
        finishAffinity()
    }

    // Cargar fichero Json del directorio de Assets:

    private suspend fun loadJsonArticulos() {

        try {
            // Obtener el InputStream del archivo de artículos en assets
            val inputStream: InputStream = assets.open("Inventario_20241111_1.articulos.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            // Convertir el byte array a String
            val json = String(buffer, StandardCharsets.UTF_8)

            // Parsear el JSON
            val jsonArray = JSONArray(json)
            val max = jsonArray.length()



            // Iterar sobre cada objeto del array JSON
            for (i in 0 until max) {
                val jsonObject = jsonArray.getJSONObject(i)

                // Extraer los valores de cada objeto JSON
                val idArticulo = jsonObject.getString("IdArticulo")
                val idCombinacion = jsonObject.getString("IdCombinacion")
                val descripcion = jsonObject.getString("Descripcion")
                val stockReal = jsonObject.getDouble("StockReal")

                dbInventario.insertarArticulo(idArticulo, idCombinacion, descripcion, stockReal)

                Log.i("Insertado artículo a DB", "Artículo ${i+1}")
                // Imprimir los valores por log
                // Log.i("Lectura de artículos", "Artículo ${i+1}: idArticulo  $idArticulo  IdCombinacion  $idCombinacion  descripcion $descripcion Unidades Stock   $stockReal")
            }

        } catch (e: Exception) {
            Log.e("TAG", "loadJson: error ${e.message}")
        }
    }

    private suspend fun loadJsonCodigosBarras() {

        try {
            // Obtener el InputStream del archivo de artículos en assets
            val inputStream: InputStream = assets.open("Inventario_20241111_1.cbarras.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            // Convertir el byte array a String
            val json = String(buffer, StandardCharsets.UTF_8)

            // Parsear el JSON
            val jsonArray = JSONArray(json)
            val max = jsonArray.length()



            // Iterar sobre cada objeto del array JSON
            for (i in 0 until max) {
                val jsonObject = jsonArray.getJSONObject(i)

                // Extraer los valores de cada objeto JSON
                val codigoBarras = jsonObject.getString("CodigoBarras")
                val idArticulo = jsonObject.getString("IdArticulo")
                val idCombinacion = jsonObject.getString("IdCombinacion")

                dbInventario.insertarCodigoBarras(codigoBarras, idArticulo, idCombinacion)


                Log.i("Insertado código barras a DB", "Código Barras ${i+1}")

                // Imprimir los valores por log
                //Log.i("Lectura de códigos de barras", "Código ${i+1}: códigoBarras: $codigoBarras  idArticulo  $idArticulo  IdCombinacion  $idCombinacion")
            }

        } catch (e: Exception) {
            Log.e("TAG", "loadJson: error ${e.message}")
        }
    }

    private suspend fun loadJsonPartidas() {

        try {
            // Obtener el InputStream del archivo de artículos en assets
            val inputStream: InputStream = assets.open("Inventario_20241111_1.partidasnserie.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            // Convertir el byte array a String
            val json = String(buffer, StandardCharsets.UTF_8)

            // Parsear el JSON
            val jsonArray = JSONArray(json)
            val max = jsonArray.length()

            // Iterar sobre cada objeto del array JSON
            for (i in 0 until max) {
                val jsonObject = jsonArray.getJSONObject(i)

                // Extraer los valores de cada objeto JSON
                val idArticulo = jsonObject.getString("IdArticulo")
                val partida = jsonObject.getString("Partida")
                val fechaCaducidad = jsonObject.getString("FCaducidad")
                val numeroSerie = jsonObject.getString("NSerie")

                dbInventario.insertarPartida(partida, idArticulo, fechaCaducidad, numeroSerie)

                Log.i("Insertada partida a DB", "Partida ${i+1}")
            }

        } catch (e: Exception) {
            Log.e("TAG", "loadJson: error ${e.message}")
        }
    }
}
