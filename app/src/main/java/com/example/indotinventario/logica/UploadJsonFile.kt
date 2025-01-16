package com.example.indotinventario.logica

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.util.Log
import com.example.indotinventario.api.ConexionAPI
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UploadJsonFile {

    companion object{

        private lateinit var COD_EMPRESA:String

        suspend fun saveJsonInventario(context: Context, dbInventario:DBInventario, dbUsuarios: DBUsuarios) {

            try {
                // Cursor para obtener el usuario de la sesión y el código de su empresa para guardar el fichero:
                val todosUsuariosCursor: Cursor = dbUsuarios.obtenerTodosUsuarios()
                // Se crea el cursor para obtener todos los ítems de la tabla inventario:
                val todosItemsCursor: Cursor = dbInventario.obtenerTodosItemInventario()

                // Crear un array JSON que contendrá todas los ítems del inventario:
                val itemsInventarioJsonArray = JSONArray()

                if (todosItemsCursor.moveToFirst()) {
                    // Indices de las columnas del cursor
                    // val codigoBarrasIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_CODIGO_BARRAS) // No se necesita código barras para el fichero Json
                    val descripcionIndex =
                        todosItemsCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)
                    val idArticuloIndex =
                        todosItemsCursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                    val idCombinacionIndex =
                        todosItemsCursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                    val partidaIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_PARTIDA)
                    val fechaCaducidadIndex =
                        todosItemsCursor.getColumnIndex(DBInventario.COLUMN_FECHA_CADUCIDAD)
                    val numeroSerieIndex =
                        todosItemsCursor.getColumnIndex(DBInventario.COLUMN_NUMERO_SERIE)
                    val unidadesContadasIndex =
                        todosItemsCursor.getColumnIndex(DBInventario.COLUMN_UNIDADES_CONTADAS)

                    // Iterar sobre cada fila del cursor
                    do {
                        // Crear un nuevo JSONObject en cada iteración
                        val itemJson = JSONObject()

                        //val codigoBarras = todosItemsCursor.getString(codigoBarrasIndex) // No se necesita código barras para el fichero Json
                        val descripcion = todosItemsCursor.getString(descripcionIndex)
                        val idArticulo = todosItemsCursor.getString(idArticuloIndex)
                        val idCombinacion = todosItemsCursor.getString(idCombinacionIndex)
                        val partida = todosItemsCursor.getString(partidaIndex)
                        val fechaCaducidad = todosItemsCursor.getString(fechaCaducidadIndex)
                        val numeroSerie = todosItemsCursor.getString(numeroSerieIndex)
                        val unidadesContadas = todosItemsCursor.getDouble(unidadesContadasIndex)

                        //itemJson.put("codigoBarras", codigoBarras) // No se necesita código barras para el fichero Json
                        itemJson.put("descripcion", descripcion)
                        itemJson.put("idArticulo", idArticulo)
                        itemJson.put("idCombinacion", idCombinacion)
                        itemJson.put("partida", partida)
                        itemJson.put("fechaCaducidad", fechaCaducidad)
                        itemJson.put("numeroSerie", numeroSerie)
                        itemJson.put("unidadesContadas", unidadesContadas)

                        // Añadir el objeto JSON al array
                        itemsInventarioJsonArray.put(itemJson)

                    } while (todosItemsCursor.moveToNext())
                } else {
                    Log.i("Json Inventario", "No se encontró ningún elemento de inventario")
                }
                // Se cierra la conexión a la DB:
                dbInventario.close()

                if (todosUsuariosCursor.moveToFirst() && itemsInventarioJsonArray.length() > 0) {

                    val codEmpresaIndex = todosUsuariosCursor.getColumnIndex(DBUsuarios.COLUMN_COD_EMPRESA)
                    COD_EMPRESA = todosUsuariosCursor.getString(codEmpresaIndex)

                    dbUsuarios.close()
                    val fileName = "${COD_EMPRESA}_Inventario.items.json" //Queremos que se sobreescriba en API un fichero de inventario de la misma empresa

                    //Almacenamiento externo, carpeta de la app:
                    val externalStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

                    val fichero = File(externalStorageDir, fileName)

                    val outputStream = FileOutputStream(fichero)
                    outputStream.write(itemsInventarioJsonArray.toString().toByteArray())
                    outputStream.close()
                    ConexionAPI.uploadFile(fichero)
                } else {
                    Log.i("Cod_Empresa", "No se encontró ningún código de empresa")
                }
                }catch(e:Exception){
                Log.e("TAG", "Error al guardar el archivo JSON: ${e.message}")
            }
        }

        fun obtenerFechaActual(): String {

            val formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val fechaActual = Date() // Obtener la fecha y hora actual
            return formatoFecha.format(fechaActual) // Formatear la fecha en el formato deseado y devolverla como String
        }

        fun obtenerHoraActual(): String {
            // Definir el formato para la hora
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            val horaActual = Date() // Obtener la fecha y hora actual
            return formatoHora.format(horaActual) // Formatear la hora y devolverla como String
        }
    }
}