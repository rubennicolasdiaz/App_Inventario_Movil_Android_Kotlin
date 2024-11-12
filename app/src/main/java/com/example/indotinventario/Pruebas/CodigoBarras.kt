package com.example.indotinventario.Pruebas

import java.io.Serializable

data class CodigoBarras(

    var codigoBarras:String,
    var idArticulo:String,
    var idCombinacion:String
) : Serializable


