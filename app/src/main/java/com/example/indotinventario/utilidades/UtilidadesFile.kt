package com.example.indotinventario.utilidades

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object UtilidadesFile {

    fun createMultipartFile(file: File): MultipartBody.Part {

        val requestFile: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }
}