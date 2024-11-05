package com.example.indotinventario.Pruebas

class Tabla4(
    var idArticulo: String,
    var idCombinacion: String,
    var partida: String,
    var fechaCaducidad: String,
    var numeroSerie: String,
    var unidadesContadas: String
) {

    fun updateUnidadesContadas(unidades: String) {
        this.unidadesContadas = if (unidades.equals("0")) "0" else unidades
    }
}

