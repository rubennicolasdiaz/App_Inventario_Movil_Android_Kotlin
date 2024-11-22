package com.example.indotinventario

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.indotinventario.Pruebas.Articulo
import com.example.indotinventario.adapter.ArticuloAdapter
import com.example.indotinventario.databinding.ActivityRecyclerViewBinding

class BuscarDescripcionActivity : AppCompatActivity() {

    // Instancia de la clase DB Inventario
    private lateinit var dbInventario: DBInventario
    private lateinit var binding: ActivityRecyclerViewBinding

    private lateinit var listaArticulos: ArrayList<Articulo>
    private lateinit var articuloMutableList: MutableList<Articulo>


    private lateinit var adapter: ArticuloAdapter
    private val llmanager = LinearLayoutManager(this)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarDB()

        initRecyclerView()
    }

    private fun initRecyclerView() {

        adapter = ArticuloAdapter(
            listaArticulos,
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
            listaArticulos = ArrayList()
            articuloMutableList = listaArticulos.toMutableList()

            if(cursorArticulos.moveToNext()){

                do{
                    val idArticuloIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                    val idCombinacionIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                    val descripcionIndex = cursorArticulos.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)

                    val idArticulo = cursorArticulos.getString(idArticuloIndex)
                    val idCombinacion = cursorArticulos.getString(idCombinacionIndex)
                    val descripcion = cursorArticulos.getString(descripcionIndex)

                    listaArticulos.add(Articulo(idArticulo, idCombinacion, descripcion))
                    articuloMutableList = listaArticulos

                }while(cursorArticulos.moveToNext())
            }
            // Se cierra el cursor de la base de datos
            cursorArticulos.close()

        }catch(e:Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onItemSelected(articulo: Articulo) {
        //Toast.makeText(this, articulo.idArticulo, Toast.LENGTH_SHORT).show()
        buscarArticuloDescripcion(articulo)
    }

    private fun buscarArticuloDescripcion(articulo:Articulo) {

        try{

            var codigoBarras: String = ""
            var idCombinacion:String = ""
            var idArticulo = articulo.idArticulo
            var descripcion = ""
            var partida = ""
            var fechaCaducidad = ""
            var numeroSerie = ""

            val cursorCodigosBarras = dbInventario.obtenerCodigoBarrasPorArticulo(articulo.idArticulo)
            val cursorArticulo = dbInventario.obtenerArticulo(articulo.idArticulo)

            if(cursorCodigosBarras.moveToFirst()){

                do{
                    val codigoBarrasIndex = cursorCodigosBarras.getColumnIndex(DBInventario.COLUMN_CODIGO_BARRAS)
                    val idCombinacionIndex = cursorCodigosBarras.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)

                    codigoBarras = cursorCodigosBarras.getString(codigoBarrasIndex)
                    idCombinacion = cursorCodigosBarras.getString(idCombinacionIndex)

                }while(cursorCodigosBarras.moveToNext())
            }

            /// BUSCAR DESCRIPCIÓN Y HACER un cursor de partidas por si hubiera:




            // Se cierra el cursor de la base de datos
            cursorCodigosBarras.close()


            buscarPorCodigoBarras(codigoBarras, idCombinacion, idArticulo)


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


                /* dbInventario.close()


                 // Se llama a corrutina para salvar los datos de la DB a ficheros Json en almacenamiento externo:
                 lifecycleScope.launch(Dispatchers.IO){

                     // Se llama a la función async y al método await para que no se ejecute el
                     // siguiente código hasta que finalice la tarea anterior:
                     async{saveJsonArticulos(this@BuscarCodigoBarrasActivity)}.await()
                     async{saveJsonCodigosBarras(this@BuscarCodigoBarrasActivity)}.await()
                     async{saveJsonPartidas(this@BuscarCodigoBarrasActivity)}.await()

                     finishAffinity()
                 } */

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun buscarPorCodigoBarras(codigoBarras:String, idCombinacion:String, idArticulo:String){

        // Crear el Bundle y agregar el valor de codigoBarras
        val bundle = Bundle()
        bundle.putString("codigoBarras", codigoBarras)
        bundle.putString("idCombinacion", idCombinacion)
        bundle.putString("idArticulo", idArticulo)

        // Crear el Intent para pasar a la siguiente actividad
        val intent = Intent(this, BuscarCodigoBarrasActivity::class.java)
        intent.putExtras(bundle)  // Adjuntar el Bundle al Intent

        // Iniciar la siguiente actividad y terminar la actual
        startActivity(intent)
        finish()
    }
}