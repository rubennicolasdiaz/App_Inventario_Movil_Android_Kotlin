package com.example.indotinventario.Pruebas

import java.io.Serializable
import java.util.Date

data class Partida(

    var idArticulo: String,
    var partida:String,
    var fechaCaducidad:Date,
    var numeroSerie: String
) : Serializable

