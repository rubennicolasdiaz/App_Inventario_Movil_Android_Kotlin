package com.example.indotinventario.dominio

import kotlinx.serialization.*

@Serializable
data class Usuario(

    //val id: Int,
    //val nombre: String,
    var email: String,
   // val password:String,
    //val empresa:String,
    var codEmpresa:String,
    var token:String
)