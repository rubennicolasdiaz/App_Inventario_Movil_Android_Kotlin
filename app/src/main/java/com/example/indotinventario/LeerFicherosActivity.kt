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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

            if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK) {
                // Verificar si hay más de un archivo seleccionado
                val uris = mutableListOf<Uri>()

                if (data?.clipData != null) {
                    // Hay múltiples archivos
                    val clipData = data.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount ) {
                            val uri = clipData?.getItemAt(i)?.uri
                            if (uri != null) {
                                uris.add(uri)
                            }
                        }
                    }
                } else {
                    // Un solo archivo
                    val uri = data?.data
                    if (uri != null) {
                        uris.add(uri)
                    }
                }

                // Procesar cada archivo seleccionado
                for (uri in uris) {
                    var targetFile: File? = null

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
                        // Verificar si el archivo ya existe
                        if (targetFile.exists()) {
                            targetFile.delete()
                            Log.d("FileCheck", "El archivo ${targetFile.name} ya existía, se ha eliminado para sobrescribirlo.")
                        }

                        copyFile(uri, targetFile)
                    }
                }

                showAlertDialog(this@LeerFicherosActivity)

                //finish()
            }else{
                Log.e("Error:", "Error al")
                finish()
            }
        }catch (e:Exception){

            Log.e("Error:", e.message.toString())
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


        System.exit(0)  // Finaliza el proceso de la aplicación
    }

    private fun showAlertDialog(context: Context){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Reiniciar aplicación")
            .setMessage("¿Quieres reiniciar la aplicación para guardar los " +
                    "datos en la base de datos?")

            .setPositiveButton("Sí") { dialog, which ->

                reiniciarApp()

            }.setNegativeButton("No") { dialog, which ->

                dialog.dismiss()
                finish()
            }
        builder.show()
    }
}