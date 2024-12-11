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
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type
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
            val damagedUris = ArrayList<Uri>()
            val uris = ArrayList<Uri>()

            if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK) {
                // Verificar si hay más de un archivo seleccionado


                if (data?.clipData != null) { // Hay múltiples archivos

                    val clipData = data.clipData

                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i)?.uri

                            if (uri != null) {
                                if(isValidJson(uri) is Articulo ||
                                    isValidJson(uri) is Partida ||
                                    isValidJson(uri) is CodigoBarras){

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
                        if(isValidJson(uri) is Articulo ||
                            isValidJson(uri) is Partida ||
                            isValidJson(uri) is CodigoBarras){

                            uris.add(uri)}
                        else{
                            damagedUris.add(uri)
                        }
                    }
                }
            }

            if(damagedUris.size > 0){
                showErrorDialog(this@LeerFicherosActivity)

            }else{

                var targetFile: File? = null

                for (uri in uris) {
                    val validJson = isValidJson(uri)

                    if (validJson is Partida) {
                        Log.i("ValidJson", validJson.toString())
                        Log.i("ValidJson", "Entra en el if del bucle for")
                        targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "partidas.json")
                        if (targetFile.exists()) {
                            targetFile.delete()
                        }
                        copyFile(uri, targetFile)
                    }

                    if (validJson is Articulo) {
                        Log.i("ValidJson", validJson.toString())
                        Log.i("ValidJson", "Entra en el if del bucle for")
                        targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "articulos.json")
                        if (targetFile.exists()) {
                            targetFile.delete()
                        }
                        copyFile(uri, targetFile)
                    }

                    if (validJson is CodigoBarras) {
                        Log.i("ValidJson", validJson.toString())
                        Log.i("ValidJson", "Entra en el if del bucle for")
                        targetFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "cbarras.json")
                        if (targetFile.exists()) {
                            targetFile.delete()
                        }
                        copyFile(uri, targetFile)
                    }
                }
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

    private fun isValidJson(uri: Uri): Any? {
        try {

            val articulo = Articulo(
                IdArticulo = "",
                IdCombinacion = "",
                Descripcion = "",
                StockReal = 0.0
            )

            val cbarras = CodigoBarras(
                CodigoBarras = "",
                IdArticulo = "",
                IdCombinacion = ""
            )

            val partida = Partida(
                IdArticulo = "",
                Partida = "",
                FCaducidad = "",
                NSerie = "",
            )

            // Abrir el archivo de entrada
            val inputStream = contentResolver.openInputStream(uri)
            val jsonContent = inputStream?.bufferedReader().use { it?.readText() }

            // Verificar si el archivo está vacío
            if (jsonContent.isNullOrBlank()) {
                Log.i("ValidJson", "El archivo está vacío.")
            }

            // Intentar deserializar el contenido del JSON como un array
            try {
                Gson().fromJson(jsonContent, Array<Articulo>::class.java).toList()
                return articulo
            } catch (e: JsonParseException) {
                Log.i("ValidJson", "Error al deserializar Articulo: ${e.message}")

            }

            try {
                Gson().fromJson(jsonContent, Array<CodigoBarras>::class.java).toList()
                return cbarras
            } catch (e: JsonParseException) {
                Log.i("ValidJson", "Error al deserializar CodigoBarras: ${e.message}")

            }

            try {
                Gson().fromJson(jsonContent, Array<Partida>::class.java).toList()
                return partida
            } catch (e: JsonParseException) {
                Log.i("ValidJson", "Error al deserializar Partida: ${e.message}")

            }
        } catch (e: IOException) {

            Log.i("ValidJson", "Error al leer el archivo: ${e.message}")

        } catch (e: SecurityException) {

            Log.i("ValidJson", "Error de permisos: ${e.message}")

        } catch (e: Exception) {

            Log.i("ValidJson", "Error inesperado: ${e.message}")

        }
        return null
    }
}

