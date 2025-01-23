package com.example.indotinventario.dominio

import java.io.Serializable

data class Articulo(
    val IdArticulo:String?,
    val IdCombinacion:String?,
    val Descripcion:String?,
    val StockReal: Double?
) : Serializable