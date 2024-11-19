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
        const val COLUMN_STOCK_REAL = "StockReal"

        // Tabla CodigosBarras
        private const val TABLE_CODIGOS_BARRAS = "CodigosBarras"
        const val COLUMN_CODIGO_BARRAS = "CodigoBarras"

        // Tabla Partidas
        private const val TABLE_PARTIDAS = "Partidas"
        const val COLUMN_ID_PARTIDA = "IdPartida"
        const val COLUMN_PARTIDA = "Partida"
        const val COLUMN_FECHA_CADUCIDAD = "FechaCaducidad"
        const val COLUMN_NUMERO_SERIE = "NumeroSerie"


        // Tabla Inventario
        private const val TABLE_INVENTARIO = "Inventario"
        private const val COLUMN_ID_INVENTARIO = "IdInventario"

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
        // Crear tabla Articulos
        val createArticulosTable = """
            CREATE TABLE $TABLE_ARTICULOS (
                $COLUMN_ID_ARTICULO TEXT NOT NULL,
                $COLUMN_DESCRIPCION TEXT NOT NULL,
                $COLUMN_ID_COMBINACION TEXT NOT NULL,
                $COLUMN_STOCK_REAL DOUBLE,
                PRIMARY KEY ($COLUMN_ID_ARTICULO, $COLUMN_ID_COMBINACION)
            );
        """.trimIndent()

        // Crear tabla CodigosBarras
        val createCodigosBarrasTable = """
            CREATE TABLE $TABLE_CODIGOS_BARRAS (
                $COLUMN_CODIGO_BARRAS TEXT PRIMARY KEY,
                $COLUMN_ID_ARTICULO TEXT,
                $COLUMN_ID_COMBINACION TEXT,
                FOREIGN KEY($COLUMN_ID_ARTICULO) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_ARTICULO),
                FOREIGN KEY($COLUMN_ID_COMBINACION) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_COMBINACION)
            );
        """.trimIndent()

        // Crear tabla Partidas
        val createPartidasTable = """
            CREATE TABLE $TABLE_PARTIDAS (
                $COLUMN_ID_PARTIDA INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PARTIDA TEXT,
                $COLUMN_ID_ARTICULO TEXT,
                $COLUMN_ID_COMBINACION TEXT,
                $COLUMN_FECHA_CADUCIDAD TEXT,
                $COLUMN_NUMERO_SERIE TEXT,
                 
                FOREIGN KEY($COLUMN_ID_ARTICULO) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_ARTICULO) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_ID_COMBINACION) REFERENCES $TABLE_ARTICULOS($COLUMN_ID_COMBINACION) ON DELETE CASCADE
            );
        """.trimIndent()

        // Crear tabla Inventario
        val createInventarioTable = """
            CREATE TABLE $TABLE_INVENTARIO (
                $COLUMN_ID_INVENTARIO INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ID_ARTICULO TEXT NOT NULL,
                $COLUMN_ID_COMBINACION TEXT,
                $COLUMN_DESCRIPCION TEXT NOT NULL,
                $COLUMN_STOCK_REAL DOUBLE NOT NULL,
                $COLUMN_PARTIDA TEXT,
                
                $COLUMN_FECHA_CADUCIDAD TEXT,
                $COLUMN_NUMERO_SERIE TEXT,
                $COLUMN_CODIGO_BARRAS TEXT,
                FOREIGN KEY ($COLUMN_ID_ARTICULO) REFERENCES $TABLE_ARTICULOS ($COLUMN_ID_ARTICULO),
                FOREIGN KEY ($COLUMN_ID_COMBINACION) REFERENCES $TABLE_ARTICULOS ($COLUMN_ID_COMBINACION),
                FOREIGN KEY ($COLUMN_PARTIDA) REFERENCES $TABLE_PARTIDAS ($COLUMN_PARTIDA),
                FOREIGN KEY ($COLUMN_CODIGO_BARRAS) REFERENCES $TABLE_CODIGOS_BARRAS ($COLUMN_CODIGO_BARRAS)
            );
        """.trimIndent()

        // Ejecutar la creación de las tablas
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
        onCreate(db)
    }

    // Insertar un artículo
    fun insertarArticulo(idArticulo: String, idCombinacion: String?, descripcion: String, stockReal:Double) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_ID_COMBINACION, idCombinacion)
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_STOCK_REAL, stockReal)
        }
        db.insert(TABLE_ARTICULOS, null, values)
        db.close()
    }

    // Obtener un artículo por ID
    fun obtenerArticulo(idArticulo: String, idCombinacion: String?): Cursor {
        val db = this.readableDatabase
        return db.query(
            TABLE_ARTICULOS, null, "$COLUMN_ID_ARTICULO = ? AND $COLUMN_ID_COMBINACION = ?",
            arrayOf(idArticulo, idCombinacion), null, null, null
        )
    }

    // Obtener todos los artículos
    fun obtenerTodosArticulos(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_ARTICULOS", null)
    }

    // Insertar una partida
    fun insertarPartida(partida: String?, idArticulo: String, idCombinacion: String?,
                        fechaCaducidad: String?, numeroSerie: String?, stockReal: Double) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PARTIDA, partida)
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_ID_COMBINACION, idCombinacion)
            put(COLUMN_FECHA_CADUCIDAD, fechaCaducidad)
            put(COLUMN_NUMERO_SERIE, numeroSerie)

        }
        db.insert(TABLE_PARTIDAS, null, values)
        db.close()
    }

    // Obtener todas las partidas
    fun obtenerTodasPartidas(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_PARTIDAS", null)
    }

    // Obtener partidas por ID de artículo
    fun obtenerPartidaPorIdArticulo(idArticulo: String): Cursor {
        val db = this.readableDatabase
        return db.query(
            TABLE_PARTIDAS, null, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo),
            null, null, null
        )
    }

    fun actualizarStock(idArticulo: String, stockReal: Double) {
        val db = this.writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_STOCK_REAL, stockReal)

        }
        db.update(TABLE_PARTIDAS, values, "$COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo))
        db.close()
    }

    // Insertar un código de barras
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

    // Obtener todos los códigos de barras
    fun obtenerTodosCodigosdeBarras(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_CODIGOS_BARRAS", null)
    }

    // Obtener un código de barras por su valor
    fun obtenerCodigoBarras(codigoBarras: String): Cursor {
        val db = this.readableDatabase
        return db.query(
            TABLE_CODIGOS_BARRAS, null, "$COLUMN_CODIGO_BARRAS = ?", arrayOf(codigoBarras),
            null, null, null
        )
    }

    // Obtener códigos de barras por ID de artículo
    fun obtenerCodigosBarrasPorArticulo(idArticulo: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_CODIGOS_BARRAS WHERE $COLUMN_ID_ARTICULO = ?", arrayOf(idArticulo)
        )
    }

    // Insertar un registro en la tabla de inventario
    fun insertarInventario(idArticulo: String, idCombinacion: String?, descripcion: String,
                           stockReal: Double, partida: String?,
                           fechaCaducidad: String?, numeroSerie: String?, codigoBarras: String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID_ARTICULO, idArticulo)
            put(COLUMN_ID_COMBINACION, idCombinacion)
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_STOCK_REAL, stockReal)
            put(COLUMN_PARTIDA, partida)
            put(COLUMN_FECHA_CADUCIDAD, fechaCaducidad)
            put(COLUMN_NUMERO_SERIE, numeroSerie)
            put(COLUMN_CODIGO_BARRAS, codigoBarras)
        }
        db.insert(TABLE_INVENTARIO, null, values)
        db.close()
    }

    // Obtener cada elemento del Inventario
    fun obtenerTodoInventario(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_INVENTARIO", null)
    }
}
