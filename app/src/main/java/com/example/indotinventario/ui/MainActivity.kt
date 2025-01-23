package com.example.indotinventario.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import com.example.indotinventario.R
import com.example.indotinventario.api.ConexionAPI
import com.example.indotinventario.logica.DownloadJsonFiles
import com.example.indotinventario.databinding.WaitScreenMainBinding
import com.example.indotinventario.dominio.LoginApi
import com.example.indotinventario.logica.DBInventario
import com.example.indotinventario.logica.DBUsuarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast

class MainActivity : AppCompatActivity() {

    private lateinit var dbInventario: DBInventario
    private lateinit var dbUsuarios: DBUsuarios

    private lateinit var binding:ActivityMainBinding
    private lateinit var waitBinding:WaitScreenMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(3000)  // Tiempo de splash screen manual
        setTheme(R.style.AppTheme)  // Aplicar el tema correcto después del splash

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater) // Implementación de View Binding:
        waitBinding = WaitScreenMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarBotones()

        inicializarDBUsuarios()
        inicializarDBInventario()
    }

    private fun cargarBotones() {

        binding.buttonLogin.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            comprobarLogin(email, password)
        }

        binding.buttonSalir.setOnClickListener {
            cerrarAplicacion()
        }
    }

    private fun comprobarLogin(email:String, password:String){

        if (email.isEmpty() || password.isEmpty()) {

            MotionToast.createToast(
                this, "ERROR DE LOGIN",
                "Todos los campos deben estar cumplimentados",
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_CENTER,
                MotionToast.LONG_DURATION,
                null
            )
        } else {

            val loginApi = LoginApi(email, password)

            lifecycleScope.launch(Dispatchers.IO) {

                if(!ConexionAPI.loginApi(loginApi)){

                    withContext(Dispatchers.Main){

                        MotionToast.createToast(
                            this@MainActivity, "ERROR DE LOGIN",
                            "El usuario o la contraseña introducidos son incorrectos",
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_CENTER,
                            MotionToast.LONG_DURATION,
                            null
                        )
                    }

                }else{

                    withContext(Dispatchers.Main){
                        cargarVistaEspera()
                    }

                    descargarFicherosApi()
                }
            }
        }
    }

    private fun inicializarDBInventario() {

        dbInventario = DBInventario.getInstance(this@MainActivity)
        val db = dbInventario.writableDatabase

        db.execSQL("DROP TABLE IF EXISTS Articulos") //Eliminar las tablas existentes
        db.execSQL("DROP TABLE IF EXISTS CodigosBarras")
        db.execSQL("DROP TABLE IF EXISTS Partidas")
        db.execSQL("DROP TABLE IF EXISTS Inventario")

        dbInventario.onCreate(db) //Volver a crear las tablas
    }

    private fun inicializarDBUsuarios() {

        dbUsuarios = DBUsuarios.getInstance(this@MainActivity)

        val db = dbUsuarios.writableDatabase

        db.execSQL("DROP TABLE IF EXISTS Usuarios") //Eliminar las tablas existentes

        dbUsuarios.onCreate(db) //Volver a crear las tablas
    }

    private fun pasarAMenuActivity() {

        startActivity(Intent(this, MenuActivity::class.java))
        dbInventario.close()
        dbUsuarios.close()

        finish()
    }

    private suspend fun descargarFicherosApi(){

        val ficheroArticulos = ConexionAPI.getCodigoEmpresa() + "_Inventario_20241119_1.articulos.json"
        val ficheroCBarras = ConexionAPI.getCodigoEmpresa() + "_Inventario_20241119_1.cbarras.json"
        val ficheroPartidasNserie = ConexionAPI.getCodigoEmpresa() + "_Inventario_20241119_1.partidasnserie.json"

        val arrayListArticulos = ConexionAPI.downloadFileArticulos(ficheroArticulos)
        val arrayListCBarras = ConexionAPI.downloadFileCBarras(ficheroCBarras)
        val arrayListPartidasNSerie = ConexionAPI.downloadFilePartidasNSerie(ficheroPartidasNserie)

        DownloadJsonFiles.downloadJsonArticulos(dbInventario, arrayListArticulos)
        DownloadJsonFiles.downloadJsonCodigosBarras(dbInventario, arrayListCBarras)
        DownloadJsonFiles.downloadJsonPartidas(dbInventario, arrayListPartidasNSerie)

        if(DownloadJsonFiles.isDownloadOK()){

            withContext(Dispatchers.Main){
                pasarAMenuActivity()
            }
        }else{
            withContext(Dispatchers.Main) {
                showAlertDialog(this@MainActivity)
            }
        }
    }

    private fun cerrarAplicacion() {
        finishAffinity()
    }

    private fun cargarVistaEspera(){

        waitBinding.progressBar.visibility = View.VISIBLE // Progress Bar de Espera:

        // View Binding de Espera:
        setContentView(waitBinding.root)
    }

    private fun cargarVistaNormal(){

        waitBinding.progressBar.visibility = View.GONE
        setContentView(binding.root)
    }

    private fun showAlertDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("ERROR DE DESCARGA DE FICHEROS")
            .setMessage("No se han podido descargar ficheros desde la Api. Por favor, " +
                    "revisar conexión a Internet o consultar con el administrador de la Api" +
                    " Indot Inventario Móvil.")

            .setPositiveButton("ACEPTAR") { dialog, which ->


                cargarVistaNormal()
            }
        val dialog = builder.create()
        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(Color.BLUE)
    }
}
