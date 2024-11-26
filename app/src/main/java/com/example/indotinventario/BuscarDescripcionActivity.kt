package com.example.indotinventario

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.indotinventario.Pruebas.Articulo
import com.example.indotinventario.Pruebas.SaveJsonFile
import com.example.indotinventario.adapter.ArticuloAdapter
import com.example.indotinventario.databinding.ActivityBuscarDescripcionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BuscarDescripcionActivity : AppCompatActivity() {

    // Instancia de la clase DB Inventario
    private lateinit var dbInventario: DBInventario
    private lateinit var binding: ActivityBuscarDescripcionBinding

    private lateinit var articuloMutableList: MutableList<Articulo>


    private lateinit var adapter: ArticuloAdapter
    private val llmanager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityBuscarDescripcionBinding.inflate(layoutInflater)
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
                    articulo.descripcion.lowercase().contains(userFilter.toString().lowercase())
                }
            adapter.updateArticulos(articulosFiltered)
        }
    }

    private fun inicializarDB(){

        try{
            dbInventario= DBInventario.getInstance(this)
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

                    articuloMutableList.add(Articulo(idArticulo, idCombinacion, descripcion))

                }while(cursorArticulos.moveToNext())
            }
            // Se cierra el cursor de la base de datos
            cursorArticulos.close()

        }catch(e:Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onItemSelected(articulo: Articulo) {
        buscarArticuloDescripcion(articulo)
    }

    private fun buscarArticuloDescripcion(articulo:Articulo) {

        try{
            var codigoBarras: String = ""
            var idArticulo = articulo.idArticulo


            val cursorCodigosBarras = dbInventario.obtenerCodigoBarrasPorArticulo(articulo.idArticulo)

            if(cursorCodigosBarras.moveToFirst()){

                do{
                    val codigoBarrasIndex = cursorCodigosBarras.getColumnIndex(DBInventario.COLUMN_CODIGO_BARRAS)
                    codigoBarras = cursorCodigosBarras.getString(codigoBarrasIndex)

                }while(cursorCodigosBarras.moveToNext())
            }
            cursorCodigosBarras.close()
            buscarPorCodigoBarras(codigoBarras, idArticulo)
        }catch(e:Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
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

                showAlertDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun buscarPorCodigoBarras(codigoBarras:String, idArticulo:String){

        // Crear el Bundle y agregar el valor de codigoBarras
        val bundle = Bundle()
        bundle.putString("codigoBarras", codigoBarras)
        bundle.putString("idArticulo", idArticulo)

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
                    "inventario y salir de la app?")

            .setPositiveButton("Sí") { dialog, which ->

                dbInventario.close()

                // Se llama a corrutina para salvar el inventario de la DB a un fichero Json en almacenamiento externo:
                lifecycleScope.launch(Dispatchers.IO){

                    async{ SaveJsonFile.saveJsonInventario(this@BuscarDescripcionActivity, dbInventario)}.await()
                    finishAffinity() // Finaliza la app.
                }

            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
    }
}