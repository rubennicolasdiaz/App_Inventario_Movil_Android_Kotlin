package com.example.indotinventario.api

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.indotinventario.api.Responses.DownloadFileResponseArticulo
import com.example.indotinventario.api.Responses.DownloadFileResponseCBarras
import com.example.indotinventario.api.Responses.DownloadFileResponsePartidasNSerie
import com.example.indotinventario.utilidades.UtilidadesFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import www.sanju.motiontoast.MotionToast
import java.io.File

object ConexionAPI {

    private const val URL_NGROK = "https://dfc2-2-139-239-88.ngrok-free.app" //
    private const val API_BASE_URL: String = "/apiindot/"
    var TOKEN_KEY = "JWT_TOKEN"

    private var llamadaArticulos:Boolean = false
    private var llamadaCBarras:Boolean = false
    private var llamadaPartidas:Boolean = false

    private fun getRetrofit(): Retrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl(URL_NGROK + API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient())
            .build()
        return retrofit
    }

    private fun getClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor()) // Aquí se llama a Header Interceptor para meter Token en cabecera
            .build()
        return client
    }

    suspend fun loginApi(loginApi: LoginApi, context: Context): String {
        val service: ApiService = getRetrofit().create(ApiService::class.java)
        try {
            val response = service.loginApi(loginApi)

            if (response.isSuccessful) {
                val token = response.body()?.token

                withContext(Dispatchers.Main) {
                    if (token != null) {
                        TOKEN_KEY = token
                    }
                }
            } else {

                    Log.e("API Error", "Error en la llamada a la API")

            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                MotionToast.createToast(
                    context as Activity,
                    "Error en la solicitud",
                    "Ha habido un problema durante el login a la API",
                    MotionToast.TOAST_ERROR,
                    MotionToast.GRAVITY_CENTER,
                    MotionToast.SHORT_DURATION,
                    null
                )
            }
        }
        return TOKEN_KEY
    }

    //SUBIR FICHERO:
    suspend fun uploadFile(file: File):Boolean {

        val multipart = UtilidadesFile.createMultipartFile(file)
        val service: ApiService = getRetrofit().create(ApiService::class.java)
        val response = service.uploadFile(multipart)

        if (response.isSuccessful) {

           Log.i("upload", "fichero subido correctamente")

            return true
        } else {

            Log.i("upload", "Error en la llamada a la Api")
        }
        return false
    }

    //DESCARGAR FICHERO ARTICULOS:
    suspend fun downloadFileArticulos(nombreFichero:String):DownloadFileResponseArticulo? {

        var listArticulos:DownloadFileResponseArticulo? = null

        val service: ApiService = getRetrofit().create(ApiService::class.java)
        val response = service.downloadFileArticulos(nombreFichero)

        if (response.isSuccessful) {
            llamadaArticulos = true
            Log.i("download", "Fichero desgargado con éxito")
            listArticulos = response.body()
        } else {

            Log.i("download","Error al descargar el archivo")
        }
        return listArticulos
    }

    //DESCARGAR FICHERO CÓDIGOS DE BARRAS:
    suspend fun downloadFileCBarras(nombreFichero:String):DownloadFileResponseCBarras? {

        var listCBarras:DownloadFileResponseCBarras? = null

        val service: ApiService = getRetrofit().create(ApiService::class.java)
        val response = service.downloadFileCBarras(nombreFichero)

        if (response.isSuccessful) {
            llamadaCBarras = true
            Log.i("download", "Fichero desgargado con éxito")
            listCBarras = response.body()
        } else {

            Log.i("download","Error al descargar el archivo")
        }

        return listCBarras
    }

    //DESCARGAR FICHERO PARTIDAS Y NÚMEROS DE SERIE:
    suspend fun downloadFilePartidasNSerie(nombreFichero:String):DownloadFileResponsePartidasNSerie? {

        var listPartidas:DownloadFileResponsePartidasNSerie? = null

        val service: ApiService = getRetrofit().create(ApiService::class.java)
        val response = service.downloadFilePartidas(nombreFichero)

        if (response.isSuccessful) {
            llamadaPartidas = true

            Log.i("download", "Fichero desgargado con éxito")

            listPartidas = response.body()
        } else {

            Log.i("download","Error al descargar el archivo")
        }
        return listPartidas
    }

    fun comprobarLlamadasApi():Boolean{

        if(llamadaArticulos && llamadaCBarras && llamadaPartidas){
            return true
        }else{
            return false
        }
    }
}


