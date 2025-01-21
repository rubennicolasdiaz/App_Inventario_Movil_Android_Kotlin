package com.example.indotinventario.api

import com.example.indotinventario.api.Responses.DownloadFileResponseArticulo
import com.example.indotinventario.api.Responses.DownloadFileResponseCBarras
import com.example.indotinventario.api.Responses.DownloadFileResponsePartidasNSerie
import com.example.indotinventario.api.Responses.LoginResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    //LOGIN EN API PARA RECIBIR TOKEN JWT
    @POST("loginapi")
    suspend fun loginApi(@Body loginApi: LoginApi): Response<LoginResponse>

    //SUBIR UN SÓLO FICHERO:
    @Multipart
    @POST("upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<ResponseBody>

    //DESCARGAR FICHERO ARTICULOS
    @POST("download")
    suspend fun downloadFileArticulos(@Query("nombreFichero") nombreFichero: String): Response<DownloadFileResponseArticulo>

    //DESCARGAR FICHERO CBARRAS
    @POST("download")
    suspend fun downloadFileCBarras(@Query("nombreFichero") nombreFichero: String): Response<DownloadFileResponseCBarras>

    //DESCARGAR FICHERO PARTIDAS Y NÚMEROS SERIE
    @POST("download")
    suspend fun downloadFilePartidas(@Query("nombreFichero") nombreFichero: String): Response<DownloadFileResponsePartidasNSerie>
}