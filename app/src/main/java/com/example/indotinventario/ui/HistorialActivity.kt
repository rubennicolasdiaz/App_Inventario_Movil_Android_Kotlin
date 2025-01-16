package com.example.indotinventario.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.indotinventario.R
import com.example.indotinventario.dominio.InventarioItem
import com.example.indotinventario.logica.UploadJsonFile
import com.example.indotinventario.adapter.ItemAdapter
import com.example.indotinventario.databinding.ActivityHistorialBinding
import com.example.indotinventario.logica.DBInventario
import com.example.indotinventario.logica.DBUsuarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast

class HistorialActivity : AppCompatActivity() {

    // Instancia de la clase DBInventario
    private lateinit var dbInventario: DBInventario
    private lateinit var binding: ActivityHistorialBinding

    // Variable para acceder a la DB de Usuarios:
    private lateinit var dbUsuarios: DBUsuarios
    private var inventarioMutableList: MutableList<InventarioItem> = mutableListOf()

    private lateinit var adapter: ItemAdapter
    private val llmanager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se inicializa la DB:
        inicializarDB()

        // Se cargan los componentes gráficos de la vista:
        initRecyclerView()
    }

    private fun initRecyclerView() {

        adapter = ItemAdapter(
            inventarioMutableList,
            onClickListener = { inventarioItem -> onItemSelected(inventarioItem) },
            onClickDelete = { position -> showConfirmDeleteItem(this, position) }
        )
        binding.recyclerHistorial.layoutManager = llmanager
        binding.recyclerHistorial.adapter = adapter


////////////////// CÓDIGO NUEVO PARA FILTRAR

        binding.etFilter.addTextChangedListener { userFilter ->

            // Convertir el filtro a minúsculas para no ser sensible a mayúsculas y minúsculas
            val filterText = userFilter.toString().lowercase()

            // Filtrar los artículos basados en el tipo de filtro
            val articulosFiltered = if (filterText.any { it.isDigit() }) {
                // Si el filtro contiene algún número, filtrar por código de barras
                inventarioMutableList.filter { inventarioItem ->
                    inventarioItem.codigoBarras.lowercase().contains(filterText)
                }
            } else {
                // Si el filtro contiene solo letras, filtrar por descripción
                inventarioMutableList.filter { inventarioItem ->
                    inventarioItem.descripcion.lowercase().contains(filterText)
                }
            }

            // Actualizar el adaptador con la lista filtrada
            adapter.updateArticulos(articulosFiltered)
        }
    }

    private fun inicializarDB(){

        try{
            dbInventario= DBInventario.getInstance(this)
            dbUsuarios = DBUsuarios.getInstance(this)
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

                    inventarioMutableList.add(
                        InventarioItem(codigoBarras, descripcion, idArticulo, idCombinacion, partida,
                        fechaCaducidad, numeroSerie, unidadesContadas)
                    )

                }while(cursorInventario.moveToNext())
            }else{

                MotionToast.createToast(this,"INVENTARIO",
                    "No hay ningún inventario registrado",
                    MotionToast.TOAST_INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    null)
            }
            // Se cierra el cursor de la base de datos
            cursorInventario.close()

        }catch(e:Exception){

            MotionToast.createToast(this,"INVENTARIO",
                "No hay ningún inventario registrado",
                MotionToast.TOAST_INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                null)
        }
    }

    private fun onItemSelected(inventarioItem: InventarioItem) {

    }

    private fun onDeletedItem(position: Int) {

        val itemToDelete: InventarioItem = inventarioMutableList[position]

        val idArticulo = itemToDelete.idArticulo
        val idCombinacion = itemToDelete.idCombinacion
        val partida = itemToDelete.partida
        val numeroSerie = itemToDelete.numeroSerie

        try{

            dbInventario.deleteItemInventario(idArticulo, idCombinacion,
                partida, numeroSerie)

            inventarioMutableList.removeAt(position)
            adapter.notifyItemRemoved(position)


            MotionToast.createToast(this,"INVENTARIO",
                "Elemento eliminado correctamente",
                MotionToast.TOAST_DELETE,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                null)
        }catch(e:Exception){

            MotionToast.createToast(this,"ERROR INVENTARIO",
                e.message.toString(),
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                null)
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

                    async{ UploadJsonFile.saveJsonInventario(this@HistorialActivity, dbInventario,dbUsuarios)}.await()
                }
                finishAffinity() // Finaliza la app.
            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
            }
        builder.show()
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
}