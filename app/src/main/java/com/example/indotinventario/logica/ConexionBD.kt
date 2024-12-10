package com.example.indotinventario.logica

import android.os.StrictMode
import android.widget.EditText
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class ConexionBD {

    // Esta clase venía en la versión de Java que se abandonó en 2021, no se usa para nada.

    private val connectionUrl = "jdbc:jtds:sqlserver://192.168.1.41:1433;databaseName=PruebaDB;integratedSecurity=true;user=sa;password=1234;"

    private fun conexionBD(): Connection? {
        return try {
            // Permitir operaciones en el hilo principal (no recomendado en producción)
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance()
            val conexion = DriverManager.getConnection(connectionUrl)
            println("Connection Successful!")
            conexion
        } catch (e: Exception) {
            System.err.println(e)
            null
        }
    }

    fun verCosas(login: EditText, pass: EditText): Boolean {
        val query = "SELECT Login, Password FROM CfgSgUsuarios"
        return try {
            conexionBD()?.prepareStatement(query)?.use { ps ->
                val r: ResultSet = ps.executeQuery()
                while (r.next()) {
                    println("${r.getString(1)} - - - ${r.getString(2)}")
                    if (r.getString(1) == login.text.toString() && r.getString(2) == pass.text.toString()) {
                        return true
                    }
                }
            }
            false
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }
}
