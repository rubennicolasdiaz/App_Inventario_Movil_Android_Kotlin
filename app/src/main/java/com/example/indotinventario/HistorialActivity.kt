package com.example.indotinventario

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.indotinventario.Pruebas.InventarioItem
import com.example.indotinventario.adapter.ItemAdapter
import com.example.indotinventario.databinding.ActivityHistorialBinding

class HistorialActivity : AppCompatActivity() {

    // Instancia de la clase DBInventario
    private lateinit var dbInventario: DBInventario
    private lateinit var binding: ActivityHistorialBinding

    private var articuloMutableList: MutableList<InventarioItem> = mutableListOf()

    private lateinit var adapter: ItemAdapter
    private val llmanager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se cargan los componentes gráficos de la vista:
        initRecyclerView()

        // Se inicializa la DB:
        inicializarDB()
    }

    private fun initRecyclerView() {

        adapter = ItemAdapter(
            articuloMutableList,
            onClickListener = { inventarioItem -> onItemSelected(inventarioItem) }

        )
        binding.recyclerArticulo.layoutManager = llmanager
        binding.recyclerArticulo.adapter = adapter
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

    private fun inicializarDB(){

        try{
            dbInventario= DBInventario.getInstance(this)

            val cursorInventario = dbInventario.obtenerTodosItemInventario()

            if(cursorInventario.moveToNext()){

                do{
                    val codigoBarrasIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_CODIGO_BARRAS)
                    val descripcionIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)
                    val idArticuloIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                    val idCombinacionIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                    val partidaIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_PARTIDA)
                    val fechaCaducidadIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_FECHA_CADUCIDAD)
                    val numeroSerieIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_NUMERO_SERIE)
                    val unidadesContadasIndex = cursorInventario.getColumnIndex(DBInventario.COLUMN_UNIDADES_CONTADAS)

                    val codigoBarras = cursorInventario.getString(codigoBarrasIndex)
                    val descripcion = cursorInventario.getString(descripcionIndex)
                    val idArticulo = cursorInventario.getString(idArticuloIndex)
                    val idCombinacion = cursorInventario.getString(idCombinacionIndex)
                    val partida = cursorInventario.getString(partidaIndex)
                    val fechaCaducidad = cursorInventario.getString(fechaCaducidadIndex)
                    val numeroSerie = cursorInventario.getString(numeroSerieIndex)
                    val unidadesContadas = cursorInventario.getString(unidadesContadasIndex)


                    articuloMutableList.add(InventarioItem(codigoBarras, descripcion, idArticulo, idCombinacion, partida,
                        fechaCaducidad, numeroSerie, unidadesContadas))

                }while(cursorInventario.moveToNext())
            }else{

                Toast.makeText(this, "No hay ningún inventario registrado", Toast.LENGTH_LONG).show()
            }
            // Se cierra el cursor de la base de datos
            cursorInventario.close()


        }catch(e:Exception){
            Toast.makeText(this, "No hay ningún inventario registrado", Toast.LENGTH_LONG).show()
        }
    }

    private fun onItemSelected(inventarioItem: InventarioItem) {

    }
}