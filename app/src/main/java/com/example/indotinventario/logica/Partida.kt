package com.example.indotinventario.logica

import java.io.Serializable

data class Partida(
    val IdArticulo: String,
    val Partida: String,
    val FCaducidad: String,
    val NSerie: String
) : Serializable