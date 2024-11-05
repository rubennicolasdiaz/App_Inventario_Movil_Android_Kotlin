package com.example.indotinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.indotinventario.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Animación de Inicio de la Aplicación:
        val screenSplash = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        screenSplash.setKeepOnScreenCondition{true}

        almacenamientoInternoFicheros(this)
        cargarVista()
    }

    // Se inicializan los elementos de la vista:
    private fun cargarVista() {

        binding.etUsuario
        binding.etContrasena

        binding.buttonLogin.setOnClickListener {
            comprobarLogin()
        }

        binding.buttonSalir.setOnClickListener {
            cerrarAplicacion()
        }
    }

    //---LOGIN--------------------------------------------------------------------------------------

    private fun comprobarLogin() {

        pasarAMenuActivity()

        // De momento para las pruebas dejamos el login sin funcionalidad

        /*
        val con = ConsultaWS(etUsuario.text.toString().trim(), etContraseña.text.toString().trim())

        if (con.verCosas(etUsuario, etContraseña)) {
            crearCarpetas()
            crearJSON()
            startActivity(Intent(applicationContext, MenuActivity::class.java))
        } else {
            Toast.makeText(this, R.string.error_login, Toast.LENGTH_SHORT).show()
        }
        */
    }

    //---CREAR-ARCHIVOS-DE-PRUEBA-------------------------------------------------------------------

    private fun crearJSON() {
        // Uncomment to use
        /*
        guardarJSON("""
            [
              {"CodigoBarras": "1", "IdArticulo": "1", "IdCombinacion": "1"},
              {"CodigoBarras": "2", "IdArticulo": "2", "IdCombinacion": "2"},
              {"CodigoBarras": "3", "IdArticulo": "3", "IdCombinacion": "3"},
              {"CodigoBarras": "4", "IdArticulo": "4", "IdCombinacion": "4"},
              {"CodigoBarras": "5901234123457", "IdArticulo": "5", "IdCombinacion": "5"}
            ]
        """.trimIndent(), "InventarioAlmacen1.json")

        guardarJSON("""
            [
              {"IdArticulo": "1", "IdCombinacion": "1", "Descripcion": "Primer numero", "StockReal": "120"},
              {"IdArticulo": "2", "IdCombinacion": "2", "Descripcion": "Segundo", "StockReal": "60"},
              {"IdArticulo": "3", "IdCombinacion": "3", "Descripcion": "Tercer numero", "StockReal": "40"},
              {"IdArticulo": "4", "IdCombinacion": "4", "Descripcion": "Cuarto", "StockReal": "30"},
              {"IdArticulo": "5", "IdCombinacion": "5", "Descripcion": "Prueba semi-real", "StockReal": "24"}
            ]
        """.trimIndent(), "InventarioAlmacen2.json")

        guardarJSON("""
            [
              {"IdArticulo": "1", "Partida": "10", "Fcaducidad": "01/02/1998", "NSerie": "001"},
              {"IdArticulo": "2", "Partida": "20", "Fcaducidad": "11/05/2007", "NSerie": "002"},
              {"IdArticulo": "3", "Partida": "30", "Fcaducidad": "07/02/2020", "NSerie": "003"},
              {"IdArticulo": "4", "Partida": "40", "Fcaducidad": "23/12/2025", "NSerie": "004"},
              {"IdArticulo": "5", "Partida": "50", "Fcaducidad": "14/05/2037", "NSerie": "005"}
            ]
        """.trimIndent(), "InventarioAlmacen3.json")
        */
    }

    private fun almacenamientoInternoFicheros(context: Context) {
        // Define la ruta de la carpeta dentro del directorio de archivos de la aplicación
        val rutaCarpeta = File(Environment.getExternalStorageDirectory(), "InventarioIndot/EntradaDatos")

        // Verifica si la carpeta existe
        if (!rutaCarpeta.exists()) {
            // Si no existe, intenta crearla
            if (rutaCarpeta.mkdirs()) {
                Toast.makeText(context, "Carpeta 'ficheros' creada: ${rutaCarpeta.absolutePath}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error al crear la carpeta.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "La carpeta 'ficheros' ya existe: ${rutaCarpeta.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    private fun pasarAMenuActivity() {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    private fun cerrarAplicacion() {
        finishAffinity()
    }
}
