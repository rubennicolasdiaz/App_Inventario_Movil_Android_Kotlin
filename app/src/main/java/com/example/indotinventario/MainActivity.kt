package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import com.example.indotinventario.logica.LoadJsonFile
import com.example.indotinventario.databinding.WaitScreenMainBinding
import com.example.indotinventario.logica.DBInventario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast

class MainActivity : AppCompatActivity() {

    private lateinit var dbInventario: DBInventario
    private lateinit var binding:ActivityMainBinding
    private lateinit var waitBinding:WaitScreenMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // Dormimos el hilo principal 2 segundos para que se vea el screen y se ejecute la corrutina
        // para leer los Json y pasar los datos a SQLite:
        Thread.sleep(2000)
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        waitBinding = WaitScreenMainBinding.inflate(layoutInflater)
        setContentView(waitBinding.root)

        // Se inicializa la DB:
        inicializarDB()

        //Corrutina para volcar datos de los ficheros Json a la SQLite:
        lifecycleScope.launch(Dispatchers.IO){

            withContext(Dispatchers.Main) {
                waitBinding.progressBar.visibility = View.VISIBLE
            }

            //Se ejecuta asíncronamente la lectura de ficheros Json y el volcado a SQLite
            async { loadJsonFiles() }.await()

            withContext(Dispatchers.Main) {
                // Implementación de View Binding:
                binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(binding.root)

                // Se cargan los componentes gráficos de la vista:
                cargarVista()
            }
        }
    }

    private fun cargarVista() {

        binding.buttonLogin.setOnClickListener {
            comprobarLogin()
        }

        binding.buttonSalir.setOnClickListener {
            cerrarAplicacion()
        }
    }

    private fun comprobarLogin() {

        // El login debería realizarse contra una base de datos o un fichero interno de credenciales.

        val usuario = binding.etUsuario.text.toString()
        val password = binding.etPassword.text.toString()

        if(usuario.isBlank() || password.isEmpty()){

            MotionToast.createToast(this,
                "ERROR LOGIN",
                "Ninguno de los campos puede estar vacío",
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_CENTER,
                MotionToast.SHORT_DURATION,
                null)

        }else{

            pasarAMenuActivity()
        }
    }

    private fun inicializarDB() {

        dbInventario = DBInventario.getInstance(this)

        // Obtener la base de datos en modo escritura
        val db = dbInventario.writableDatabase

        // Eliminar las tablas existentes
        db.execSQL("DROP TABLE IF EXISTS Articulos")
        db.execSQL("DROP TABLE IF EXISTS CodigosBarras")
        db.execSQL("DROP TABLE IF EXISTS Partidas")
        db.execSQL("DROP TABLE IF EXISTS Inventario")

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

    private suspend fun loadJsonFiles(){

            LoadJsonFile.loadJsonArticulos(dbInventario, this@MainActivity)
            LoadJsonFile.loadJsonCodigosBarras(dbInventario, this@MainActivity)
            LoadJsonFile.loadJsonPartidas(dbInventario, this@MainActivity)
    }
}
