package com.example.indotinventario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.indotinventario.databinding.ActivityLeerFicherosBinding
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LeerFicherosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeerFicherosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Implementación de View Binding:
        binding = ActivityLeerFicherosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarVista()
    }

    private fun cargarVista() {
        binding.buttonLeerArchivos.setOnClickListener {

            val uri = Uri.parse(Environment.DIRECTORY_DCIM)
            openFile(uri)
        }
    }

    // Request code for selecting a file.
    val PICK_JSON_FILE = 2

    fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Para todos los tipos de fichero
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, PICK_JSON_FILE)
    }

    // Este método manejará el resultado del archivo seleccionado
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var targetFile: File? = null
        if (requestCode == PICK_JSON_FILE && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            if(uri.toString().contains("partidas", ignoreCase = true)){

                // Definir el archivo de destino
                targetFile = File((this@LeerFicherosActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)), "partidas.json")

                // Verificar si el archivo ya existe
                if (targetFile.exists()) {
                    // Si el archivo existe, eliminarlo para sobrescribirlo
                    targetFile.delete()
                    Log.d("FileCheck", "El archivo ${targetFile.name} ya existía, se ha eliminado para sobrescribirlo.")
                }
                copyFile(uri, targetFile)
            }else if(uri.toString().contains("articulos", ignoreCase = true)){

                // Definir el archivo de destino
                targetFile = File((this@LeerFicherosActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)), "articulos.json")

                // Verificar si el archivo ya existe
                if (targetFile.exists()) {
                    // Si el archivo existe, eliminarlo para sobrescribirlo
                    targetFile.delete()
                    Log.d("FileCheck", "El archivo ${targetFile.name} ya existía, se ha eliminado para sobrescribirlo.")
                }

                copyFile(uri, targetFile)
            }else if(uri.toString().contains("cbarras", ignoreCase = true)){

                // Definir el archivo de destino
                targetFile = File((this@LeerFicherosActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)), "cbarras.json")

                // Verificar si el archivo ya existe
                if (targetFile.exists()) {
                    // Si el archivo existe, eliminarlo para sobrescribirlo
                    targetFile.delete()
                    Log.d("FileCheck", "El archivo ${targetFile.name} ya existía, se ha eliminado para sobrescribirlo.")
                }
                copyFile(uri, targetFile)
            }
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
}
