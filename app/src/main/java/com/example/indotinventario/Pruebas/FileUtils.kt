package com.example.indotinventario.Pruebas

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    private const val LOG_TAG = "FileUtils"

    private var contentUri: Uri? = null

    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        var selection: String? = null
        var selectionArgs: Array<String>? = null

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]

                    val fullPath = getPathFromExtSD(split.toTypedArray())
                    if (fullPath.isNotEmpty()) {
                        return fullPath
                    }
                }
                isDownloadsDocument(uri) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val cursor: Cursor? = context.contentResolver.query(
                            uri,
                            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )
                        cursor?.use {
                            if (it.moveToFirst()) {
                                val fileName = it.getString(0)
                                val path = "${Environment.getExternalStorageDirectory()}/Download/$fileName"
                                if (path.isNotEmpty()) {
                                    return path
                                }
                            }
                        }
                        val id = DocumentsContract.getDocumentId(uri)
                        if (id.isNotEmpty()) {
                            if (id.startsWith("raw:")) {
                                return id.replaceFirst("raw:", "")
                            }
                            val contentUriPrefixesToTry = arrayOf(
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                            )
                            for (contentUriPrefix in contentUriPrefixesToTry) {
                                try {
                                    val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), id.toLong())
                                    return getDataColumn(context, contentUri, null, null)
                                } catch (e: NumberFormatException) {
                                    return uri.path?.replaceFirst("^/document/raw:", "")?.replaceFirst("^raw:", "")
                                }
                            }
                        }
                    } else {
                        val id = DocumentsContract.getDocumentId(uri)
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "")
                        }
                        try {
                            contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), id.toLong()
                            )
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                        if (contentUri != null) {
                            return getDataColumn(context, contentUri!!, null, null)
                        }
                    }
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]

                    val contentUri: Uri? = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])

                    return contentUri?.let { getDataColumn(context, it, selection, selectionArgs) }
                }
                isGoogleDriveUri(uri) -> {
                    return getDriveFilePath(uri, context)
                }
            }
        } else if ("content".equals(uri.scheme)) {
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context)
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                return getMediaFilePathForN(uri, context)
            } else {
                return getDataColumn(context, uri, null, null)
            }
        } else if ("file".equals(uri.scheme)) {
            return uri.path
        }
        return null
    }

    private fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    private fun getPathFromExtSD(pathData: Array<String>): String {
        val type = pathData[0]
        val relativePath = "/${pathData[1]}"
        var fullPath = ""

        if ("primary".equals(type, ignoreCase = true)) {
            fullPath = "${Environment.getExternalStorageDirectory()}$relativePath"
            if (fileExists(fullPath)) {
                return fullPath
            }
        }

        fullPath = "${System.getenv("SECONDARY_STORAGE")}$relativePath"
        if (fileExists(fullPath)) {
            return fullPath
        }

        fullPath = "${System.getenv("EXTERNAL_STORAGE")}$relativePath"
        if (fileExists(fullPath)) {
            return fullPath
        }

        return fullPath
    }

    private fun getDriveFilePath(uri: Uri, context: Context): String? {
        val contentResolver: ContentResolver = context.contentResolver
        var returnCursor: Cursor? = null

        return try {
            returnCursor = contentResolver.query(uri, null, null, null, null)

            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            returnCursor?.moveToFirst()

            val name = returnCursor?.getString(nameIndex) ?: "tempfile"
            val file = File(context.cacheDir, name)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }
            file.path
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message ?: "Error writing file")
            null
        } finally {
            returnCursor?.close()
        }
    }


    private fun getMediaFilePathForN(uri: Uri, context: Context): String? {
        val returnCursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
        returnCursor?.moveToFirst()

        val name = returnCursor?.getString(nameIndex)
        val file = File(context.filesDir, name ?: "tempfile")
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }
            file.path
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message ?: "Error writing file")
            null
        }
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        val cursor: Cursor? = context.contentResolver.query(uri, arrayOf("_data"), selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndexOrThrow("_data")
                return it.getString(index)
            }
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority

    private fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority

    private fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority

    private fun isGooglePhotosUri(uri: Uri) = "com.google.android.apps.photos.content" == uri.authority

    private fun isGoogleDriveUri(uri: Uri) =
        "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
}
