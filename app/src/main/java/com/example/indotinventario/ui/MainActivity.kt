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
import com.example.indotinventario.api.LoginApi
import com.example.indotinventario.logica.DownloadJsonFiles
import com.example.indotinventario.databinding.WaitScreenMainBinding
import com.example.indotinventario.logica.ConexionSupabase
import com.example.indotinventario.logica.DBInventario
import com.example.indotinventario.logica.DBUsuarios
import com.example.indotinventario.dominio.Usuario
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast

class MainActivity : AppCompatActivity() {

    private lateinit var supabase: SupabaseClient //Cliente Supabase para login en tabla de "usuarios"
    private lateinit var dbInventario: DBInventario
    private lateinit var dbUsuarios: DBUsuarios

    private var codEmpresa:String = ""
    private var loginApi: LoginApi = LoginApi("", "")

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

        inicializarSupabase()
    }

    private fun cargarBotones() {

        binding.buttonLogin.setOnClickListener {
            comprobarLogin()
        }

        binding.buttonSalir.setOnClickListener {
            cerrarAplicacion()
        }
    }

    private fun comprobarLogin() {

        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if(email.isEmpty() || password.isEmpty()){

            MotionToast.createToast(
                this@MainActivity, "ERROR DE LOGIN",
                "Todos los campos deben estar cumplimentados",
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_CENTER,
                MotionToast.LONG_DURATION,
                null
            )
        }else{

            lifecycleScope.launch(Dispatchers.IO) {

                withContext(Dispatchers.Main){
                    cargarVistaEspera()
                }

                if (comprobarUsuarioSupabase(email, password)) {

                    descargarFicherosApi()
                } else {
                    withContext(Dispatchers.Main) {

                        cargarVistaNormal()
                        MotionToast.createToast(
                            this@MainActivity, "ERROR DE LOGIN",
                            "El usuario o la contraseña introducidos son incorrectos",
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_CENTER,
                            MotionToast.LONG_DURATION,
                            null
                        )
                    }
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

        dbUsuarios.onCreate(db) // Volver a crear las tablas
    }

    private fun inicializarSupabase(){

        supabase = ConexionSupabase.getSupabaseClient()
    }

    private fun pasarAMenuActivity() {

        startActivity(Intent(this, MenuActivity::class.java))
        dbInventario.close()
        dbUsuarios.close()

        lifecycleScope.launch(Dispatchers.IO) { //Se llama a corrutina, el login en supabase es suspend fun
            supabase.close()
        }
        finish()
    }

    private suspend fun descargarFicherosApi(){

        val ficheroArticulos = codEmpresa + "_Inventario_20241119_1.articulos.json"
        val ficheroCBarras = codEmpresa + "_Inventario_20241119_1.cbarras.json"
        val ficheroPartidasNserie = codEmpresa + "_Inventario_20241119_1.partidasnserie.json"

        if(ConexionAPI.comprobarLlamadasApi()){

            withContext(Dispatchers.Main) {
                showAlertDialog(this@MainActivity)
            }
        }else{
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
    }

    private fun cerrarAplicacion() {
        finishAffinity()
    }

    private suspend fun comprobarUsuarioSupabase(email: String, password: String): Boolean {

        val listaUsuarios = ConexionSupabase.loginSupabase(supabase)

        listaUsuarios.forEach() { usuario ->
            if (usuario.email == email && usuario.password == password) {

                codEmpresa = usuario.codEmpresa
                loginApi.email = usuario.email
                loginApi.password = usuario.password
                usuario.token = obtenerTokenApi(loginApi, this@MainActivity)

                insertarUsuario(dbUsuarios, usuario)
                return true
            }
        }
        return false
    }

    private suspend fun obtenerTokenApi(loginApi: LoginApi, context:Context):String{//LLAMAR A LA API Y OBTENER TOKEN

        val token = ConexionAPI.loginApi(loginApi, context)
        return token
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

    private fun insertarUsuario(dbUsuarios: DBUsuarios, usuario: Usuario){

        dbUsuarios.insertarUsuario(usuario.id, usuario.nombre, usuario.email, usuario.password,
            usuario.empresa, usuario.codEmpresa, usuario.token)
    }

    private fun showAlertDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("ERROR DE DESCARGA DE FICHEROS")
            .setMessage("No se han podido descargar ficheros desde la Api. Por favor, " +
                    "revisar conexión a Internet o consultar con el administrador de la Api" +
                    " Indot Inventario Móvil. Pruebe también a cerrar e iniciar de nuevo la app.")

            .setPositiveButton("ACEPTAR") { dialog, which ->


                cargarVistaNormal()
            }
        val dialog = builder.create()
        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(Color.BLUE)
    }
}
