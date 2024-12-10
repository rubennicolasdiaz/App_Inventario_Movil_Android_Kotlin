package com.example.indotinventario.logica

data class InventarioItem(

    val codigoBarras:String,
    val descripcion:String,
    val idArticulo:String,
    val idCombinacion:String,
    val partida:String,
    val fechaCaducidad:String,
    val numeroSerie:String,
    val unidadesContadas:String
)