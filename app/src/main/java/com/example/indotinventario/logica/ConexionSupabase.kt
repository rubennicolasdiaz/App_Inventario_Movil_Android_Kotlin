package com.example.indotinventario.logica

import android.util.Log
import com.example.indotinventario.dominio.Usuario
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class ConexionSupabase {

    companion object{

        const val TABLA_USUARIOS = "usuarios"
        const val SUPABASE_URL = "https://zxcbtkvsfdkaqijxrpsq.supabase.co"
        const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp4Y2J0a3ZzZmRrYXFpanhycHNxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY4NDEwNDEsImV4cCI6MjA1MjQxNzA0MX0.DCEJHMvJ1qtXsC5kyp3iln9B5RbBgHQnpEafML-Qpgw"

        fun getSupabaseClient():SupabaseClient{

            return createSupabaseClient(

                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Postgrest)
            }
        }

        suspend fun loginSupabase(supabaseClient: SupabaseClient): List<Usuario>{

            var listaUsuarios: List<Usuario> = emptyList()
            try{
                listaUsuarios = supabaseClient.postgrest[TABLA_USUARIOS].select().decodeList<Usuario>()
            }catch(e:Exception){
                Log.e("ERROR_SUPABASE",e.message.toString())

            }
            return listaUsuarios
        }
    }
}