package com.example.indotinventario.Pruebas

import java.io.Serializable

data class Articulo(
    var idArticulo: String,
    var idCombinacion: String,
    var descripcion: String,
    var stockReal: Int
) : Serializable

