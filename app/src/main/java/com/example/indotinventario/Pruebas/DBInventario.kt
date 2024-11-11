package com.example.indotinventario

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBInventario (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "inventario.db"
        private const val DATABASE_VERSION = 1

        // Tabla Articulos
        private const val TABLE_ARTICULOS = "Articulos"
        const val COLUMN_ID_ARTICULO = "IdArticulo"
        const val COLUMN_DESCRIPCION = "Descripcion"
        const val COLUMN_STOCK_REAL = "StockReal"
        const val COLUMN_ID_COMBINACION = "IdCombinacion"

        // Tabla CodigosBarras
        private const val TABLE_CODIGOS_BARRAS = "CodigosBarras"
        private const val COLUMN_CODIGO_BARRAS = "CodigoBarras"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createArticulosTable = """
            CREATE TABLE $TABLE_ARTICULOS (
                $COLUMN_ID_ARTICULO TEXT PRIMARY KEY,
                $COLUMN_DESCRIPCION TEXT NOT NULL,
                $COLUMN_STOCK_REAL INTEGER NOT NULL,
                $COLUMN_ID_COMBINACION TEXT
            );
        """.trimIndent()

        val createCodigosBarrasTable = """
            CREATE TABLE $TABLE_CODIGOS_BARRAS (
                $COLUMN_CODIGO_BARRAS TEXT PRIMARY KEY,
                $COLUMN_ID_ARTICULO TEXT,
                $COLUMN_ID_COMBINACION TEXT,
                FOREIGN KEY($COLUMN_ID_ARTICULO) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_ARTICULO)
            );
        """.trimIndent()



        db.execSQL(createArticulosTable)
        db.execSQL(createCodigosBarrasTable)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ARTICULOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CODIGOS_BARRAS")

    }

    // CRUD para Artículos
    fun insertarArticulo(idArticulo: String, descripcion: String, stockReal: Int, idCombinacion: String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_STOCK_REAL, stockReal)
            put(COLUMN_ID_COMBINACION, idCombinacion)
        }
        db.insert(TABLE_ARTICULOS, null, values)
        db.close()
    }

    fun obtenerArticulo(idArticulo: String): Cursor {

        val db = this.readableDatabase
        return db.query(
            TABLE_ARTICULOS, null, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo),
            null, null, null
        )
    }

    fun obtenerTodosArticulos(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_ARTICULOS", null)
    }

    fun actualizarArticulo(idArticulo: String, descripcion: String, stockReal: Int, idCombinacion: String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_STOCK_REAL, stockReal)
            put(COLUMN_ID_COMBINACION, idCombinacion)
        }
        db.update(TABLE_ARTICULOS, values, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo))
        db.close()
    }

    fun eliminarArticulo(idArticulo: String) {
        val db = this.writableDatabase
        db.delete(TABLE_ARTICULOS, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo))
        db.close()
    }

    // CRUD para Códigos de Barras
    fun insertarCodigoBarras(codigoBarras: String, idArticulo: String, idCombinacion: String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CODIGO_BARRAS, codigoBarras)
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_ID_COMBINACION, idCombinacion)
        }
        db.insert(TABLE_CODIGOS_BARRAS, null, values)
        db.close()
    }

    fun obtenerCodigoBarras(codigoBarras: String): Cursor {

        val db = this.readableDatabase
        return db.query(
            TABLE_CODIGOS_BARRAS, null, "$COLUMN_CODIGO_BARRAS = ?", arrayOf(codigoBarras),
            null, null, null
        )
    }

    fun obtenerCodigosBarrasPorArticulo(idArticulo: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_CODIGOS_BARRAS WHERE $COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo)
        )
    }

    fun actualizarStock(idArticulo: String, stockReal: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_STOCK_REAL, stockReal)

        }
        db.update(TABLE_ARTICULOS, values, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo))
        db.close()
    }

    fun eliminarCodigoBarras(codigoBarras: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CODIGOS_BARRAS, "$COLUMN_CODIGO_BARRAS = ?", arrayOf(codigoBarras))
        db.close()
    }
}