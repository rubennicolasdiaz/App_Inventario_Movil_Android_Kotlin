package com.example.indotinventario.logica

import android.util.Log
import com.example.indotinventario.utilidades.Constantes
import com.example.indotinventario.api.Responses.DownloadFileResponseArticulo
import com.example.indotinventario.api.Responses.DownloadFileResponseCBarras
import com.example.indotinventario.api.Responses.DownloadFileResponsePartidasNSerie
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException

class DownloadJsonFiles {

    companion object{

        suspend fun loadJsonArticulos(dbInventario:DBInventario, listaArticulos: DownloadFileResponseArticulo?) {

            try {
                if(listaArticulos != null) {

                    listaArticulos.forEach{ articulo ->

                        val idArticulo = articulo.IdArticulo
                        val idCombinacion = articulo.IdCombinacion
                        val descripcion = articulo.Descripcion

                        dbInventario.insertarArticulo(idArticulo, idCombinacion, descripcion)
                    }
                }else{
                    Log.i("Ficheros", "No hay artículos en la lista")
                }
            } catch (e: Exception) {
                Log.e("Ficheros", "Error en el formato JSON: ${e.message}")
            }
        }

        suspend fun loadJsonCodigosBarras(dbInventario:DBInventario, listaCBarras: DownloadFileResponseCBarras?) {

            try {
                if(listaCBarras != null) {

                    listaCBarras.forEach{ cbarras ->

                        val codigoBarras = cbarras.CodigoBarras
                        val idArticulo = cbarras.IdArticulo
                        val idCombinacion = cbarras.IdCombinacion

                        dbInventario.insertarCodigoBarras(codigoBarras, idArticulo, idCombinacion)
                    }
                }else{
                    Log.i("Ficheros", "No hay CBarras en la lista")
                }
            } catch (e: Exception) {
                Log.e("Ficheros", "Error en el formato JSON: ${e.message}")
            }
        }

        suspend fun loadJsonPartidas(dbInventario:DBInventario, listaPartidasNserie: DownloadFileResponsePartidasNSerie?) {

            try {
                if(listaPartidasNserie != null) {

                    listaPartidasNserie.forEach { partidanserie ->

                        val idArticulo = partidanserie.IdArticulo ?: Constantes.NULL
                        val partida = partidanserie.Partida ?: Constantes.NULL
                        val fechaCaducidad = partidanserie.FCaducidad ?: Constantes.NULL
                        val numeroSerie = partidanserie.NSerie ?: Constantes.NULL

                        dbInventario.insertarPartida(idArticulo, partida, fechaCaducidad, numeroSerie)
                    }

                    val sizeArticulos = dbInventario.obtenerTodosArticulos().count.toString()
                    val sizeCBarras = dbInventario.obtenerTodosCodigosdeBarras().count.toString()
                    val sizePartidasNSerie = dbInventario.obtenerTodasPartidas().count.toString()


                    Log.i("Ficheros", "Articulos: $sizeArticulos")
                    Log.i("Ficheros", "CBarras: $sizeCBarras")
                    Log.i("Ficheros", "PartidasNSerie: $sizePartidasNSerie")

                }else{
                    Log.i("Ficheros", "No hay artículos en la lista")
                }
            } catch (e: JsonSyntaxException) {
                Log.e("Ficheros", "Error de sintaxis en el JSON: ${e.message}")
            } catch (e: JsonParseException) {
                Log.e("Ficheros", "Error al parsear el JSON: ${e.message}")
            } catch (e: Exception) {
                Log.e("Ficheros", "Error desconocido al manejar el JSON: ${e.message}")
            }
        }
    }
}