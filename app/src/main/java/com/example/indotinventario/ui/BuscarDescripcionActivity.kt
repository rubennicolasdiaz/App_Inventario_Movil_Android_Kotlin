package com.example.indotinventario.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.indotinventario.R
import com.example.indotinventario.dominio.Articulo
import com.example.indotinventario.logica.UploadWriteJsonFile
import com.example.indotinventario.adapter.ArticuloAdapter
import com.example.indotinventario.databinding.ActivityBuscarDescripcionBinding
import com.example.indotinventario.logica.DBInventario
import com.example.indotinventario.logica.DBUsuarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast

class BuscarDescripcionActivity : AppCompatActivity() {

    private lateinit var dbInventario: DBInventario
    private lateinit var dbUsuarios: DBUsuarios
    private lateinit var articuloMutableList: MutableList<Articulo>

    private lateinit var binding: ActivityBuscarDescripcionBinding
    private lateinit var adapter: ArticuloAdapter
    private val llmanager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuscarDescripcionBinding.inflate(layoutInflater) // Implementación de View Binding:
        setContentView(binding.root)

        inicializarDB()
        initRecyclerView()
    }

    private fun initRecyclerView() {

        adapter = ArticuloAdapter(
            articuloMutableList,
            onClickListener = { articulo -> onItemSelected(articulo) }

        )
        binding.recyclerArticulo.layoutManager = llmanager
        binding.recyclerArticulo.adapter = adapter

        binding.etFilter.addTextChangedListener { userFilter ->
            val articulosFiltered =
                articuloMutableList.filter { articulo ->
                    articulo.Descripcion!!.lowercase().contains(userFilter.toString().lowercase())
                }
            adapter.updateArticulos(articulosFiltered)
        }
    }

    private fun inicializarDB(){

        try{
            dbInventario= DBInventario.getInstance(this)
            dbUsuarios = DBUsuarios.getInstance(this)

            val cursorArticulos = dbInventario.obtenerTodosArticulos()
            articuloMutableList = ArrayList()

            if(cursorArticulos.moveToNext()){

                do{
                    val idArticuloIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                    val idCombinacionIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                    val descripcionIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)

                    val idArticulo = cursorArticulos.getString(idArticuloIndex)
                    val idCombinacion = cursorArticulos.getString(idCombinacionIndex)
                    val descripcion = cursorArticulos.getString(descripcionIndex)

                    articuloMutableList.add(Articulo(idArticulo, idCombinacion, descripcion, 0.0))

                }while(cursorArticulos.moveToNext())
            }else{
                MotionToast.createToast(this,"INVENTARIO",
                    "No hay ningún artículo registrado en la aplicación",
                    MotionToast.TOAST_INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    null)
            }

            cursorArticulos.close() //Se cierra el cursor de la base de datos

        }catch(e:Exception){

            MotionToast.createToast(this,"ERROR AL BUSCAR",
                "No se obtuvo ningún resultado",
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                null)
        }
    }

    private fun onItemSelected(articulo: Articulo) {
        buscarArticuloDescripcion(articulo)
    }

    private fun buscarArticuloDescripcion(articulo: Articulo) {

        try{
            var codigoBarras = ""
            val idArticulo = articulo.IdArticulo
            val idCombinacion = articulo.IdCombinacion

            val cursorCodigosBarras = dbInventario.obtenerCodigoBarrasPorArticulo(articulo.IdArticulo!!)

            if(cursorCodigosBarras.moveToFirst()){

                do{
                    val codigoBarrasIndex = cursorCodigosBarras.getColumnIndex(DBInventario.COLUMN_CODIGO_BARRAS)
                    codigoBarras = cursorCodigosBarras.getString(codigoBarrasIndex)

                }while(cursorCodigosBarras.moveToNext())
            }
            cursorCodigosBarras.close()
            if (idArticulo != null && idCombinacion != null) {
                buscarPorCodigoBarras(codigoBarras, idArticulo, idCombinacion)
            }
        }catch(e:Exception){

            MotionToast.createToast(this,"ERROR AL BUSCAR",
                e.message.toString(),
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Se sobreescribe el menú de los 3 puntitos

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

    private fun buscarPorCodigoBarras(codigoBarras:String, idArticulo:String, idCombinacion:String){

        // Crear el Bundle y agregar el valor de codigoBarras
        val bundle = Bundle()
        bundle.putString("codigoBarras", codigoBarras)
        bundle.putString("idArticulo", idArticulo)
        bundle.putString("idCombinacion", idCombinacion)

        // Crear el Intent para pasar a la siguiente actividad
        val intent = Intent(this, BuscarCodigoBarrasActivity::class.java)
        intent.putExtras(bundle)  // Adjuntar el Bundle al Intent

        // Iniciar la siguiente actividad y terminar la actual
        startActivity(intent)
        dbInventario.close()
        finish()
    }

    private fun showAlertDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar salida")
            .setMessage("¿Estás seguro de que quieres guardar los cambios del " +
                    "inventario en la nube y salir de la app?")

            .setPositiveButton("Sí") { dialog, which ->

                dbInventario.close()

                // Se llama a corrutina para salvar el inventario de la DB a un fichero Json en almacenamiento externo:
                lifecycleScope.launch(Dispatchers.IO){

                    if(dbInventario.obtenerTodosItemInventario().count <= 0){

                        withContext(Dispatchers.Main){
                            MotionToast.createToast(this@BuscarDescripcionActivity,
                                "SIN UNIDADES GUARDADAS",
                                "No se han guardado unidades de ningún artículo",
                                MotionToast.TOAST_WARNING,
                                MotionToast.GRAVITY_CENTER,
                                MotionToast.SHORT_DURATION,
                                null)
                        }
                    }else{

                        async{UploadWriteJsonFile.uploadJsonInventario(this@BuscarDescripcionActivity, dbInventario, dbUsuarios)}.await()

                        if(UploadWriteJsonFile.isUploadOK()){

                            finishAffinity() // Finaliza la app.
                        }else {
                            withContext(Dispatchers.Main) {
                                MotionToast.createToast(
                                    this@BuscarDescripcionActivity,
                                    "ERROR DE SUBIDA DE FICHERO",
                                    "Revisar conexión a Internet o consultar con el administrador de la Api",
                                    MotionToast.TOAST_ERROR,
                                    MotionToast.GRAVITY_CENTER,
                                    MotionToast.SHORT_DURATION,
                                    null
                                )
                            }
                        }
                    }
                }
            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
    }
}