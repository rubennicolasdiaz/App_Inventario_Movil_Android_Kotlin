package com.example.indotinventario.Pruebas

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.util.Log
import com.example.indotinventario.DBInventario
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaveJsonFile {
    companion object{

        suspend fun saveJsonInventario(context: Context, dbInventario:DBInventario) {

          try {
              // Se crea el cursor para obtener todos los ítems de la tabla inventario:
              val todosItemsCursor: Cursor = dbInventario.obtenerTodosItemInventario()

              // Crear un array JSON que contendrá todas los ítems del inventario:
              val itemsInventarioJsonArray = JSONArray()

              if (todosItemsCursor.moveToFirst()) {
                  // Indices de las columnas del cursor
                  val codigoBarrasIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_CODIGO_BARRAS)
                  val descripcionIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_DESCRIPCION)
                  val idArticuloIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_ID_ARTICULO)
                  val idCombinacionIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_ID_COMBINACION)
                  val partidaIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_PARTIDA)
                  val fechaCaducidadIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_FECHA_CADUCIDAD)
                  val numeroSerieIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_NUMERO_SERIE)
                  val unidadesContadasIndex = todosItemsCursor.getColumnIndex(DBInventario.COLUMN_UNIDADES_CONTADAS)

                  // Iterar sobre cada fila del cursor
                  do {
                      // Crear un nuevo JSONObject en cada iteración
                      val itemJson = JSONObject()

                      val codigoBarras = todosItemsCursor.getString(codigoBarrasIndex)
                      val descripcion = todosItemsCursor.getString(descripcionIndex)
                      val idArticulo = todosItemsCursor.getString(idArticuloIndex)
                      val idCombinacion = todosItemsCursor.getString(idCombinacionIndex)
                      val partida = todosItemsCursor.getString(partidaIndex)
                      val fechaCaducidad = todosItemsCursor.getString(fechaCaducidadIndex)
                      val numeroSerie = todosItemsCursor.getString(numeroSerieIndex)
                      val unidadesContadas = todosItemsCursor.getDouble(unidadesContadasIndex)

                      itemJson.put("codigoBarras", codigoBarras)
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

              val fileName = "Inventario_${obtenerFechaActual()}_${obtenerHoraActual()}.items.json"

              // Guardar el archivo JSON en el almacenamiento externo
              val externalStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
              if (externalStorageDir != null) {
                  val file = File(externalStorageDir, fileName)
                  try {
                      val outputStream = FileOutputStream(file)
                      outputStream.write(itemsInventarioJsonArray.toString().toByteArray())
                      outputStream.close()
                      Log.d("TAG", "Archivo JSON guardado en almacenamiento externo: ${file.absolutePath}")
                  } catch (e: IOException) {
                      Log.e("TAG", "Error al guardar el archivo JSON: ${e.message}")
                  }
              } else {
                  Log.e("TAG", "No se pudo acceder al directorio de almacenamiento externo.")
              }
          } catch (e: Exception) {
              Log.e("TAG", "Error al cargar los artículos: ${e.message}")
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