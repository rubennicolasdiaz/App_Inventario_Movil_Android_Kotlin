package com.example.indotinventario.logica

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.JsonParseException
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class LoadJsonFile {

    companion object{

        suspend fun loadJsonArticulos(dbInventario:DBInventario, context: Context) {

            try {
                // Definir la ruta al archivo en getExternalFilesDir
                val ficheroOrigen = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "articulos.json")


                // Comprobar si el archivo existe
                if (ficheroOrigen.exists()) {
                    // Abrir un InputStream desde el archivo en getExternalFilesDir
                    val inputStream: InputStream = ficheroOrigen.inputStream()

                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()

                    // Convertir el byte array a String
                    val json = String(buffer, StandardCharsets.UTF_8)

                    // Parsear el JSON
                    val jsonArray = JSONArray(json)
                    val max = jsonArray.length()

                    // Iterar sobre cada objeto del array JSON
                    for (i in 0 until max) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // Extraer los valores de cada objeto JSON
                        val idArticulo = jsonObject.getString("IdArticulo")
                        val idCombinacion = jsonObject.getString("IdCombinacion")
                        val descripcion = jsonObject.getString("Descripcion")

                        dbInventario.insertarArticulo(idArticulo, idCombinacion, descripcion)
                    }
                }else{
                    Log.i("Fichero Origen", "Fichero no encontrado")
                }
            } catch (e: JsonParseException) {
                Log.e("Ficheros", "Error en el formato JSON: ${e.message}")
            } catch (e: IOException) {
                Log.e("Ficheros", "Error de entrada/salida: ${e.message}")
            } catch (e: SecurityException) {
                Log.e("Ficheros", "Acceso no autorizado: ${e.message}")
            }
        }

        suspend fun loadJsonCodigosBarras(dbInventario:DBInventario, context: Context) {

            try {
                // Definir la ruta al archivo en getExternalFilesDir
                val ficheroOrigen = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "cbarras.json")


                // Comprobar si el archivo existe
                if (ficheroOrigen.exists()) {
                    // Abrir un InputStream desde el archivo en getExternalFilesDir
                    val inputStream: InputStream = ficheroOrigen.inputStream()

                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()

                    // Convertir el byte array a String
                    val json = String(buffer, StandardCharsets.UTF_8)

                    // Parsear el JSON
                    val jsonArray = JSONArray(json)
                    val max = jsonArray.length()

                    // Iterar sobre cada objeto del array JSON
                    for (i in 0 until max) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // Extraer los valores de cada objeto JSON
                        val codigoBarras = jsonObject.getString("CodigoBarras")
                        val idArticulo = jsonObject.getString("IdArticulo")
                        val idCombinacion = jsonObject.getString("IdCombinacion")

                        dbInventario.insertarCodigoBarras(codigoBarras, idArticulo, idCombinacion)
                    }
                }else{
                    Log.i("Fichero Origen", "Fichero no encontrado")
                }
            } catch (e: JsonParseException) {
                Log.e("Ficheros", "Error en el formato JSON: ${e.message}")
            } catch (e: IOException) {
                Log.e("Ficheros", "Error de entrada/salida: ${e.message}")
            } catch (e: SecurityException) {
                Log.e("Ficheros", "Acceso no autorizado: ${e.message}")
            }
        }

        suspend fun loadJsonPartidas(dbInventario:DBInventario, context: Context) {

            try {
                // Definir la ruta al archivo en getExternalFilesDir
                val ficheroOrigen = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "partidas.json")

                // Comprobar si el archivo existe
                if (ficheroOrigen.exists()) {
                    // Abrir un InputStream desde el archivo en getExternalFilesDir
                    val inputStream: InputStream = ficheroOrigen.inputStream()
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()

                    // Convertir el byte array a String
                    val json = String(buffer, StandardCharsets.UTF_8)

                    // Parsear el JSON
                    val jsonArray = JSONArray(json)
                    val max = jsonArray.length()

                    // Iterar sobre cada objeto del array JSON
                    for (i in 0 until max) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // Extraer los valores de cada objeto JSON
                        val idArticulo = jsonObject.getString("IdArticulo")
                        val partida = jsonObject.getString("Partida")
                        val fechaCaducidad = jsonObject.getString("FCaducidad")
                        val numeroSerie = jsonObject.getString("NSerie")

                        dbInventario.insertarPartida(partida, idArticulo, fechaCaducidad, numeroSerie)
                    }
                }else{
                    Log.i("Fichero Origen", "Fichero no encontrado")
                }
            } catch (e: JsonParseException) {
                Log.e("Ficheros", "Error en el formato JSON: ${e.message}")
            } catch (e: IOException) {
                Log.e("Ficheros", "Error de entrada/salida: ${e.message}")
            } catch (e: SecurityException) {
                Log.e("Ficheros", "Acceso no autorizado: ${e.message}")
            }
        }
    }
}