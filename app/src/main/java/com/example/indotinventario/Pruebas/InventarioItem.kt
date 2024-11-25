package com.example.indotinventario.Pruebas

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