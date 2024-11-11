package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.indotinventario.databinding.ActivityMainBinding

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

        inicializarDB()


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
        dbInventario.insertarCodigoBarras("00000009002", "09002", "COMB11")
    }





    private fun pasarAMenuActivity() {
        startActivity(Intent(this, MenuActivity::class.java))
        dbInventario.close()
        finish()
    }

    private fun cerrarAplicacion() {
        finishAffinity()
    }
}
