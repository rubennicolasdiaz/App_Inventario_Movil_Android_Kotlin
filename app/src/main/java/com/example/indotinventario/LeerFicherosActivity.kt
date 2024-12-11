package com.example.indotinventario

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import com.example.indotinventario.logica.Articulo
import com.example.indotinventario.logica.CodigoBarras
import com.example.indotinventario.logica.Partida
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.system.exitProcess

class LeerFicherosActivity : AppCompatActivity() {

    private val PICK_JSON_FILE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = Uri.parse(Environment.DIRECTORY_DCIM)
        openFile(uri)
    }

    fun openFile(pickerInitialUri: Uri){

        try{
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // Para todos los tipos de archivo
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Permitir selección múltiple
            }

             startActivityForResult(intent, PICK_JSON_FILE)
        }catch(e:Exception){
            Log.e("Error", e.message.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try{
            val damagedUris = mutableListOf<Uri>()
            val uris = mutableListOf<Uri>()

            if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK) {
                // Verificar si hay más de un archivo seleccionado


                if (data?.clipData != null) { // Hay múltiples archivos

                    val clipData = data.clipData

                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount ) {
                            val uri = clipData.getItemAt(i)?.uri

                            if (uri != null) {
                                if(isValidJson(uri)){
                                    uris.add(uri)
                                }else{
                                    damagedUris.add(uri)
                                }
                            }
                        }
                    }
                } else { // Un solo archivo

                    val uri = data?.data
                    if (uri != null) {
                        if(isValidJson(uri)){
                            uris.add(uri)
                        }else{
                            damagedUris.add(uri)
                        }
                    }
                }
            }

            if(damagedUris.size > 0){
                showErrorDialog(this@LeerFicherosActivity)

            }else{

                Log.i("ValidJson", "1")
                var targetFile:File? = null
                Log.i("ValidJson", "2")

                for (uri in uris) {

                    Log.i("ValidJson", "3")

                    Log.i("ValidJson", uri.path.toString())
                    if (uri.toString().contains("partidas", ignoreCase = true)) {
                        targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "partidas.json")
                    }

                    if (uri.toString().contains("articulos", ignoreCase = true)) {
                        targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "articulos.json")
                    }

                    if (uri.toString().contains("cbarras", ignoreCase = true)) {
                        targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "cbarras.json")
                    }

                    if (targetFile != null) {
                        Log.i("ValidJson", "Primer paso")
                        // Verificar si el archivo ya existe
                        if (targetFile.exists()) {
                            targetFile.delete()
                            Log.d("FileCheck", "El archivo ${targetFile.name} ya existía, se ha eliminado para sobrescribirlo.")
                        }
                        Log.i("ValidJson", "Paso siguiente")
                        copyFile(uri, targetFile)
                        Log.i("ValidJson", "Fichero copiado con éxito")
                    }





                }

                Log.i("ValidJson", "Tamaño de array de Uris: ${uris.size}")
                showAlertDialog(this@LeerFicherosActivity)
            }
        }catch (e: Exception) {

            finish()
        } catch (e: IOException) {

            finish()
        } catch (e: SecurityException) {

            finish()
        }
    }

    // Función para copiar el archivo desde el URI al directorio de archivos internos
    private fun copyFile(uri: Uri, destinationFile: File) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            // Confirmación de que el archivo fue copiado
            Log.d("FileCopy", "Archivo copiado a: ${destinationFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun onBackPressed() {

        finish()
        super.onBackPressed()
    }

    private fun reiniciarApp() {

       val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)

        //System.exit(0)
        exitProcess(0)
    }

    private fun showAlertDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Reiniciar aplicación")
            .setMessage("¿Quieres reiniciar la aplicación para guardar los " +
                    "datos en la base de datos?")

            .setPositiveButton("Sí") { _, _ ->

                reiniciarApp()

            }.setNegativeButton("No") { dialog, _ ->

                dialog.dismiss()
                finish()
            }
        builder.show()
    }

    private fun showErrorDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Archivo dañado o con formato no válido")
            .setMessage("Uno o varios archivos seleccionados están dañados. Por " +
                    "favor, seleccione uno o varios archivos válidos")

            .setPositiveButton("Aceptar") { _, _ ->


                finish()

            }
        builder.show()
    }

    private fun isValidJson(uri: Uri): Boolean {
        try {
            // Abrir el archivo de entrada
            val inputStream = contentResolver.openInputStream(uri)
            val jsonContent = inputStream?.bufferedReader().use { it?.readText() }



            // Verificar si el archivo está vacío
            if (jsonContent.isNullOrBlank()) {
                Log.i("ValidJson", "El archivo está vacío.")
                return false // El archivo está vacío
            }

            // Intentar deserializar el contenido del JSON como un array
            val articuloList = try {
                Gson().fromJson(jsonContent, Array<Articulo>::class.java).toList()
            } catch (e: JsonParseException) {
                Log.i("ValidJson", "Error al deserializar Articulo: ${e.message}")
                null // Si no es un array válido de Articulo, devuelve null
            }

            val codigoBarrasList = try {
                Gson().fromJson(jsonContent, Array<CodigoBarras>::class.java).toList()
            } catch (e: JsonParseException) {
                Log.i("ValidJson", "Error al deserializar CodigoBarras: ${e.message}")
                null // Si no es un array válido de CodigoBarras, devuelve null
            }

            val partidaList = try {
                Gson().fromJson(jsonContent, Array<Partida>::class.java).toList()
            } catch (e: JsonParseException) {
                Log.i("ValidJson", "Error al deserializar Partida: ${e.message}")
                null // Si no es un array válido de Partida, devuelve null
            }

            // Si al menos uno de los arrays no es null, el JSON es válido
            if (articuloList != null || codigoBarrasList != null || partidaList != null) {
                Log.i("ValidJson", "JSON válido.")
                return true
            } else {
                Log.i("ValidJson", "El JSON no coincide con ninguna de las clases esperadas.")
                return false
            }

        } catch (e: IOException) {
            Log.i("ValidJson", "Error al leer el archivo: ${e.message}")
            return false
        } catch (e: SecurityException) {
            Log.i("ValidJson", "Error de permisos: ${e.message}")
            return false
        } catch (e: Exception) {
            Log.i("ValidJson", "Error inesperado: ${e.message}")
            return false
        }
    }

}