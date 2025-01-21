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

        private var downloadArticulosOK:Boolean = false
        private var downloadCodigosBarrasOK:Boolean = false
        private var downloadPartidasNSerieOK:Boolean = false

        suspend fun downloadJsonArticulos(dbInventario:DBInventario, listaArticulos: DownloadFileResponseArticulo?) {

            try {
                if(listaArticulos != null) {

                    listaArticulos.forEach{ articulo ->

                        val idArticulo = articulo.IdArticulo ?: Constantes.CADENA_VACIA
                        val idCombinacion = articulo.IdCombinacion ?: Constantes.CADENA_VACIA
                        val descripcion = articulo.Descripcion ?: Constantes.CADENA_VACIA

                        dbInventario.insertarArticulo(idArticulo, idCombinacion, descripcion)
                        downloadArticulosOK = true
                    }
                }else{
                    Log.i("Ficheros", "No hay artículos en la lista")
                }
            } catch (e: Exception) {
                Log.e("Ficheros", "Error en el formato JSON: ${e.message}")
            }
        }

        suspend fun downloadJsonCodigosBarras(dbInventario:DBInventario, listaCBarras: DownloadFileResponseCBarras?) {

            try {
                if(listaCBarras != null) {

                    listaCBarras.forEach{ cbarras ->

                        val codigoBarras = cbarras.CodigoBarras ?: Constantes.CADENA_VACIA
                        val idArticulo = cbarras.IdArticulo ?: Constantes.CADENA_VACIA
                        val idCombinacion = cbarras.IdCombinacion ?: Constantes.CADENA_VACIA

                        dbInventario.insertarCodigoBarras(codigoBarras, idArticulo, idCombinacion)
                        downloadCodigosBarrasOK = true
                    }
                }else{
                    Log.i("Ficheros", "No hay CBarras en la lista")
                }
            } catch (e: Exception) {
                Log.e("Ficheros", "Error en el formato JSON: ${e.message}")
            }
        }

        suspend fun downloadJsonPartidas(dbInventario:DBInventario, listaPartidasNserie: DownloadFileResponsePartidasNSerie?) {

            try {
                if(listaPartidasNserie != null) {

                    listaPartidasNserie.forEach { partidanserie ->

                        val idArticulo = partidanserie.IdArticulo ?: Constantes.CADENA_VACIA
                        val partida = partidanserie.Partida ?: Constantes.CADENA_VACIA
                        val fechaCaducidad = partidanserie.FCaducidad ?: Constantes.CADENA_VACIA
                        val numeroSerie = partidanserie.NSerie ?: Constantes.CADENA_VACIA

                        dbInventario.insertarPartida(idArticulo, partida, fechaCaducidad, numeroSerie)
                        downloadPartidasNSerieOK = true
                    }
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

        fun isDownloadOK():Boolean{

            if(downloadArticulosOK && downloadCodigosBarrasOK && downloadPartidasNSerieOK){
                return true
            }else{
                return false
            }
        }
    }
}