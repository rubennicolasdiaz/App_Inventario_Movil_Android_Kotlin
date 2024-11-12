package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.indotinventario.Pruebas.Articulo
import com.example.indotinventario.Pruebas.CodigoBarras
import com.example.indotinventario.Pruebas.Partida
import com.example.indotinventario.databinding.ActivityMainBinding
import android.util.Log
import org.json.JSONArray
import java.io.InputStream
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    // Instancia de la clase DBInventario
    private lateinit var dbInventario: DBInventario
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Se define el splash de inicio de la app
        val screenSplash = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Implementación del splash de inicio de la app:
        screenSplash.setKeepOnScreenCondition{false}

        // Se cargan los componentes gráficos de la vista:
        cargarVista()

        // Se inicializa la DB:
        inicializarDB()

        // Se cargan los ficheros Json de la carpeta Assets y se pasan a la DB:
        loadJsonArticulos()

        loadJsonCodigosBarras()

        loadJsonPartidas()
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

    // Inicializar la base de datos
    private fun inicializarDB(){

        dbInventario = DBInventario(this)



        /*
        // Inserción de artículos
        dbInventario.insertarArticulo("0330", "SERVIDOR HP COMPAQ PROLIANT ML30 Gen9", 1, "COMB01")
        dbInventario.insertarArticulo("033024", "SERVIDOR HP COMPAQ PROLIANT ML30 Gen9 SSD Pro", 1, "COMB02")
        dbInventario.insertarArticulo("0701", "ORDENADOR CENTURION INTEL I3 PRO", 2, "COMB06")
        dbInventario.insertarArticulo("0702", "ORDENADOR CENTURION INTEL I3 PRO", 19, "COMB07")
        dbInventario.insertarArticulo("0800", "ORDENADOR CENTURION INTEL I5 PRO", 2, "COMB08")
        dbInventario.insertarArticulo("0802", "ORDENADOR CENTURION INTEL I5 PRO", 1, "COMB09")
        dbInventario.insertarArticulo("09002", "ORDENADOR INTEL I7 PRO XXXXXXXXXXXXXXXXXXXXX", 1, "COMB11")
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
        dbInventario.insertarCodigoBarras("00000009002", "09002", "COMB11") */
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

   fun loadJsonArticulos() {

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

                dbInventario.insertarArticulo(idArticulo, idCombinacion, stockReal, descripcion)


            Log.i("Insertado artículo a DB", "Artículo ${i+1}")
                // Imprimir los valores por log
                // Log.i("Lectura de artículos", "Artículo ${i+1}: idArticulo  $idArticulo  IdCombinacion  $idCombinacion  descripcion $descripcion Unidades Stock   $stockReal")
            }

        } catch (e: Exception) {
            Log.e("TAG", "loadJson: error ${e.message}")
        }
   }

    fun loadJsonCodigosBarras() {

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

    fun loadJsonPartidas() {

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

            dbInventario.insertarPartida(idArticulo, partida, fechaCaducidad, numeroSerie)


                Log.i("Insertada partida a DB", "Partida ${i+1}")


            // Imprimir los valores por log
            //    Log.i("Lectura de partidas", "Partida: ${i+1}: IdArtículo: $idArticulo  Partida: $partida    Fecha Caducidad: $fechaCaducidad  Número Serie: $numeroSerie")
            }

        } catch (e: Exception) {
            Log.e("TAG", "loadJson: error ${e.message}")
        }
    }
}
