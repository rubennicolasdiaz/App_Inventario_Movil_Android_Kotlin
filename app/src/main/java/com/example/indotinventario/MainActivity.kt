package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.indotinventario.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

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

    // Login:

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


    private fun pasarAMenuActivity() {
        startActivity(Intent(this, MenuActivity::class.java))
    }

    private fun cerrarAplicacion() {
        finishAffinity()
    }
}
