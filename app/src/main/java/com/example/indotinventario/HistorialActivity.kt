package com.example.indotinventario

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.indotinventario.Pruebas.InventarioItem
import com.example.indotinventario.Pruebas.SaveJsonFile
import com.example.indotinventario.adapter.ItemAdapter
import com.example.indotinventario.databinding.ActivityHistorialBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HistorialActivity : AppCompatActivity() {

    // Instancia de la clase DBInventario
    private lateinit var dbInventario: DBInventario
    private lateinit var binding: ActivityHistorialBinding

    private var inventarioMutableList: MutableList<InventarioItem> = mutableListOf()

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
            inventarioMutableList,
            onClickListener = { inventarioItem -> onItemSelected(inventarioItem) },
            onClickDelete = { position -> showConfirmDeleteItem(this, position) }
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

                showAlertDialog(this)
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


                    inventarioMutableList.add(InventarioItem(codigoBarras, descripcion, idArticulo, idCombinacion, partida,
                        fechaCaducidad, numeroSerie, unidadesContadas))

                }while(cursorInventario.moveToNext())
            }else{

                Toast.makeText(this, "No hay ningún inventario registrado", Toast.LENGTH_SHORT).show()
            }
            // Se cierra el cursor de la base de datos
            cursorInventario.close()


        }catch(e:Exception){
            Toast.makeText(this, "No hay ningún inventario registrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onItemSelected(inventarioItem: InventarioItem) {

     }

    private fun onDeletedItem(position: Int) {

        val itemToDelete:InventarioItem = inventarioMutableList[position]

        val idArticulo = itemToDelete.idArticulo
        val idCombinacion = itemToDelete.idCombinacion
        val partida = itemToDelete.partida
        val numeroSerie = itemToDelete.numeroSerie

        try{

           dbInventario.deleteItemInventario(idArticulo, idCombinacion,
               partida, numeroSerie)

            inventarioMutableList.removeAt(position)
            adapter.notifyItemRemoved(position)

            Toast.makeText(this, "Elemento eliminado correctamente", Toast.LENGTH_SHORT).show()
        }catch(e:Exception){

            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmDeleteItem(context: Context, position:Int){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el " +
                    "elemento de inventario de la búsqueda?")

            .setPositiveButton("Sí") { dialog, which ->

                onDeletedItem(position)

            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
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

                    async{ SaveJsonFile.saveJsonInventario(this@HistorialActivity, dbInventario)}.await()
                    finishAffinity() // Finaliza la app.
                }

            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
    }
}