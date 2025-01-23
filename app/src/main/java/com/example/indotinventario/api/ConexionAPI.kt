package com.example.indotinventario.api

import android.util.Log
import com.example.indotinventario.api.Responses.DownloadFileResponseArticulo
import com.example.indotinventario.api.Responses.DownloadFileResponseCBarras
import com.example.indotinventario.api.Responses.DownloadFileResponsePartidasNSerie
import com.example.indotinventario.dominio.LoginApi
import com.example.indotinventario.utilidades.UtilidadesFile
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object ConexionAPI {

    private const val URL_NGROK = "https://3df8-2-139-239-88.ngrok-free.app" //
    private const val API_BASE_URL: String = "/apiindot/"

    private var TOKEN_API = " "
    private var COD_EMPRESA = " "

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
            .addInterceptor(HeaderInterceptor()) //Aquí se llama a Header Interceptor para meter Token en cabecera
            .build()
        return client
    }

    suspend fun loginApi(loginApi:LoginApi):Boolean{

        val service: ApiService = getRetrofit().create(ApiService::class.java)

        val response = service.loginApi(loginApi)

        if (response.isSuccessful) {

            TOKEN_API = response.body()?.token ?: ""
            COD_EMPRESA = response.body()?.codEmpresa ?: ""

            return true
        } else {
            Log.e("API Error", "Error en la llamada a la API")
        }
        return false
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
            return false
        }

    }

    //DESCARGAR FICHERO ARTICULOS:
    suspend fun downloadFileArticulos(nombreFichero:String):DownloadFileResponseArticulo? {

        var listArticulos:DownloadFileResponseArticulo? = null

        val service: ApiService = getRetrofit().create(ApiService::class.java)
        val response = service.downloadFileArticulos(nombreFichero)

        if (response.isSuccessful) {

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


            Log.i("download", "Fichero desgargado con éxito")

            listPartidas = response.body()
        } else {

            Log.i("download","Error al descargar el archivo")
        }
        return listPartidas
    }

    fun getTokenApi():String{

        return TOKEN_API
    }

    fun getCodigoEmpresa():String{

        return COD_EMPRESA
    }
}


