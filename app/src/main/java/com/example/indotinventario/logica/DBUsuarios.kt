package com.example.indotinventario.logica

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBUsuarios private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "usuarios.db"
        private const val DATABASE_VERSION = 1

        // Tabla Usuarios
        private const val TABLE_USUARIOS = "Usuarios"

        const val COLUMN_ID_USUARIO = "IdUsuario"
        const val COLUMN_NOMBRE_USUARIO = "NombreUsuario"
        const val COLUMN_EMAIL_USUARIO = "EmailUsuario"
        const val COLUMN_PASSWORD = "Password"
        const val COLUMN_NOMBRE_EMPRESA = "NombreEmpresa"
        const val COLUMN_COD_EMPRESA = "CodEmpresa"
        const val COLUMN_TOKEN = "Token"

        // Instancia Singleton
        @Volatile
        private var INSTANCE: DBUsuarios? = null

        // Función para obtener la instancia de la base de datos
        fun getInstance(context: Context): DBUsuarios {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DBUsuarios(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createUsuariosTable = """
    CREATE TABLE $TABLE_USUARIOS (
            $COLUMN_ID_USUARIO TEXT NOT NULL UNIQUE,
            $COLUMN_NOMBRE_USUARIO TEXT NOT NULL UNIQUE,
            $COLUMN_EMAIL_USUARIO TEXT NOT NULL UNIQUE,
            $COLUMN_PASSWORD TEXT NOT NULL UNIQUE,
            $COLUMN_NOMBRE_EMPRESA TEXT NOT NULL UNIQUE,
            $COLUMN_COD_EMPRESA TEXT NOT NULL UNIQUE,
            $COLUMN_TOKEN TEXT NOT NULL UNIQUE,
            PRIMARY KEY ($COLUMN_ID_USUARIO)
        );
""".trimIndent()

        db.execSQL(createUsuariosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
    }

    ////////USUARIOS/////////////////////////////////////////////////////////////////////////
    fun insertarUsuario(idUsuario: Int, nombre: String?, email: String,
                        password:String, empresa:String, codEmpresa:String, token:String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_ID_USUARIO, idUsuario)
            put(COLUMN_NOMBRE_USUARIO, nombre)
            put(COLUMN_EMAIL_USUARIO, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_NOMBRE_EMPRESA, empresa)
            put(COLUMN_COD_EMPRESA, codEmpresa)
            put(COLUMN_TOKEN, token)
        }
        db.insert(TABLE_USUARIOS, null, values)
        db.close()
    }

    fun obtenerUsuario(email: String): Cursor {

        val db = this.readableDatabase
        return db.query(
            TABLE_USUARIOS, null, "$COLUMN_EMAIL_USUARIO = ?", arrayOf(email),
            null, null, null
        )
    }

    fun obtenerTodosUsuarios(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USUARIOS", null)
    }
}
