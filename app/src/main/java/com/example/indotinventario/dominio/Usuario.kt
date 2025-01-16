package com.example.indotinventario.dominio

import kotlinx.serialization.*

@Serializable
data class Usuario(

    val id: Int,
    val nombre: String,
    val email: String,
    val password:String,
    val empresa:String,
    val codEmpresa:String,
    var token:String
)