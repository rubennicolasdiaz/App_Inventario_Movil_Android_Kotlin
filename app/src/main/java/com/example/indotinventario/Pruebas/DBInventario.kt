package com.example.indotinventario

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBInventario private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "inventario.db"
        private const val DATABASE_VERSION = 1

        // Tabla Articulos
        private const val TABLE_ARTICULOS = "Articulos"
        const val COLUMN_ID_ARTICULO = "IdArticulo"
        const val COLUMN_ID_COMBINACION = "IdCombinacion"
        const val COLUMN_DESCRIPCION = "Descripcion"


        // Tabla CodigosBarras
        private const val TABLE_CODIGOS_BARRAS = "CodigosBarras"
        const val COLUMN_CODIGO_BARRAS = "CodigoBarras"

        // Tabla Partidas
        private const val TABLE_PARTIDAS = "Partidas"
        private const val COLUMN_ID_PARTIDA = "IdPartida"
        const val COLUMN_PARTIDA = "Partida"
        const val COLUMN_FECHA_CADUCIDAD = "FechaCaducidad"
        const val COLUMN_NUMERO_SERIE = "NumeroSerie"

        // Tabla Inventario
        private const val TABLE_INVENTARIO = "Inventario"
        const val COLUMN_UNIDADES_CONTADAS = "UnidadesContadas"


        // Instancia Singleton
        @Volatile
        private var INSTANCE: DBInventario? = null

        // Función para obtener la instancia de la base de datos
        fun getInstance(context: Context): DBInventario {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DBInventario(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createArticulosTable = """
    CREATE TABLE $TABLE_ARTICULOS (
        $COLUMN_ID_ARTICULO TEXT NOT NULL,
        $COLUMN_DESCRIPCION TEXT NOT NULL,
        $COLUMN_ID_COMBINACION TEXT NOT NULL,
        PRIMARY KEY ($COLUMN_ID_ARTICULO, $COLUMN_ID_COMBINACION)
    );
""".trimIndent()


        val createCodigosBarrasTable = """
    CREATE TABLE $TABLE_CODIGOS_BARRAS (
        $COLUMN_CODIGO_BARRAS TEXT PRIMARY KEY,
        $COLUMN_ID_ARTICULO TEXT,
        $COLUMN_ID_COMBINACION TEXT,
        FOREIGN KEY($COLUMN_ID_ARTICULO) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_ARTICULO),
        FOREIGN KEY($COLUMN_ID_COMBINACION) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_COMBINACION)
    );
""".trimIndent()


        val createPartidasTable = """
    CREATE TABLE $TABLE_PARTIDAS (
        $COLUMN_ID_PARTIDA INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_PARTIDA TEXT,
        $COLUMN_ID_ARTICULO TEXT,
        $COLUMN_FECHA_CADUCIDAD TEXT,
        $COLUMN_NUMERO_SERIE TEXT,
        
        FOREIGN KEY($COLUMN_ID_ARTICULO) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_ARTICULO)
    );
""".trimIndent()

        val createInventarioTable = """
    CREATE TABLE $TABLE_INVENTARIO (
        $COLUMN_CODIGO_BARRAS TEXT,
        $COLUMN_DESCRIPCION TEXT NOT NULL,
        $COLUMN_ID_ARTICULO TEXT,
        $COLUMN_ID_COMBINACION TEXT,
        $COLUMN_PARTIDA TEXT,
        $COLUMN_FECHA_CADUCIDAD TEXT,
        $COLUMN_NUMERO_SERIE TEXT,
        $COLUMN_UNIDADES_CONTADAS DOUBLE NOT NULL,
        
        PRIMARY KEY($COLUMN_ID_ARTICULO, $COLUMN_ID_COMBINACION, $COLUMN_PARTIDA, $COLUMN_NUMERO_SERIE));
""".trimIndent()


        db.execSQL(createArticulosTable)
        db.execSQL(createCodigosBarrasTable)
        db.execSQL(createPartidasTable)
        db.execSQL(createInventarioTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS $TABLE_ARTICULOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CODIGOS_BARRAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARTIDAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVENTARIO")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUD para Artículos
    fun insertarArticulo(idArticulo: String, idCombinacion: String?, descripcion: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_ID_COMBINACION, idCombinacion)
            put(COLUMN_DESCRIPCION, descripcion)
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUD para Partidas

   fun insertarPartida(partida: String, idArticulo: String, fechaCaducidad: String?, numeroSerie:String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_PARTIDA, partida)
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_FECHA_CADUCIDAD, fechaCaducidad)
            put(COLUMN_NUMERO_SERIE, numeroSerie)
        }
        db.insert(TABLE_PARTIDAS, null, values)
        db.close()
    }

    fun obtenerTodasPartidas(): Cursor {

        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_PARTIDAS", null)
    }



    fun obtenerPartidaPorIdArticulo(idArticulo: String): Cursor {

        val db = this.readableDatabase
        return db.query(
            TABLE_PARTIDAS, null, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo),
            null, null, null
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
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

    fun obtenerTodosCodigosdeBarras(): Cursor {

        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_CODIGOS_BARRAS", null)
    }

    fun obtenerCodigoBarras(codigoBarras: String): Cursor {

        val db = this.readableDatabase
        return db.query(
            TABLE_CODIGOS_BARRAS, null, "$COLUMN_CODIGO_BARRAS = ?", arrayOf(codigoBarras),
            null, null, null
        )
    }

    fun obtenerCodigoBarrasPorArticulo(idArticulo: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_CODIGOS_BARRAS WHERE $COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo)
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUD para Inventario
    fun insertarItemInventario(codigoBarras:String, descripcion:String, idArticulo:String,
                               idCombinacion:String, partida:String, fechaCaducidad:String,
                               numeroSerie:String, unidadesContadas:Double) {

        val db = this.writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_CODIGO_BARRAS, codigoBarras)
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_ID_COMBINACION, idCombinacion)
            put(COLUMN_PARTIDA, partida)
            put(COLUMN_FECHA_CADUCIDAD, fechaCaducidad)
            put(COLUMN_NUMERO_SERIE, numeroSerie)
            put(COLUMN_UNIDADES_CONTADAS, unidadesContadas)
        }

        db.insert(TABLE_INVENTARIO, null, values)
        db.close()
    }

    fun obtenerItemInventario(idArticulo: String): Cursor {

        val db = this.readableDatabase
        return db.query(
            TABLE_INVENTARIO, null, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo),
            null, null, null
        )
    }

    fun obtenerTodosItemInventario(): Cursor {

        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_INVENTARIO", null)
    }

    fun deleteItemInventario(idArticulo: String, idCombinacion: String, partida: String, numeroSerie: String){

        val db = this.readableDatabase

        val whereClause = """
        $COLUMN_ID_ARTICULO = ? AND 
        $COLUMN_ID_COMBINACION = ? AND 
        $COLUMN_PARTIDA = ? AND 
        $COLUMN_NUMERO_SERIE = ?
        """.trimIndent()

        val whereArgs = arrayOf(idArticulo, idCombinacion, partida, numeroSerie)

        // Ejecutar la consulta de eliminación
        db.delete(TABLE_INVENTARIO, whereClause, whereArgs)
    }
}
