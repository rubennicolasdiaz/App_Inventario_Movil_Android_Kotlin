package com.example.indotinventario.Pruebas

import android.os.AsyncTask
import android.util.Log

/*
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE


class ConsultaWS(private val username: String, private val password: String) : AsyncTask<String, Void, String>() {

    private var jsonResult: String = ""

    fun getJsonResult(): String {
        return jsonResult
    }



    override fun doInBackground(vararg strings: String): String {


        val NAMESPACE = "http://tempuri.org/"
        val URL = "http://10.0.2.2:44395/WebService.asmx"
        val METHOD_NAME = "login"
        val SOAP_ACTION = "http://tempuri.org/login"

        val request = SoapObject(NAMESPACE, METHOD_NAME)

        // Enviamos los datos al WebService
        request.addProperty("nombre", username)
        request.addProperty("clave", password)

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = true
            setOutputSoapObject(request)
        }

        val transporte = HttpTransportSE(URL).apply {
            debug = true
        }

        return try {
            transporte.call(SOAP_ACTION, envelope)

            // Obtener el resultado
            val resSoap = envelope.response as SoapObject

            for (i in 0 until resSoap.propertyCount) {
                // Asigna los resultados a la variable index de posición en el XML
                val ic = resSoap.getProperty(i) as SoapObject
                jsonResult = ic.getProperty(0).toString()
            }

            Log.e("Resultado", "Posible éxito")
            jsonResult
        } catch (e: Exception) {
            Log.e("Resultado", e.message ?: "Error desconocido")
            jsonResult
        }
    }
}
 */