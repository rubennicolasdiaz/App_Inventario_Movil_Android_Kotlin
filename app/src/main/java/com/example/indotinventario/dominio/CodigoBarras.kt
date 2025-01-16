package com.example.indotinventario.dominio

import java.io.Serializable

data class CodigoBarras(
    val CodigoBarras:String,
    val IdArticulo:String,
    val IdCombinacion:String,
) : Serializable