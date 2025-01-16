package com.example.indotinventario.logica

import com.example.indotinventario.dominio.Usuario
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class ConexionSupabase {

    companion object{

        fun getSupabaseClient():SupabaseClient{

            return createSupabaseClient(

                supabaseUrl = "https://zxcbtkvsfdkaqijxrpsq.supabase.co",
                supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp4Y2J0a3ZzZmRrYXFpanhycHNxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY4NDEwNDEsImV4cCI6MjA1MjQxNzA0MX0.DCEJHMvJ1qtXsC5kyp3iln9B5RbBgHQnpEafML-Qpgw"
            ) {
                install(Postgrest)
            }
        }

        suspend fun loginSupabase(supabaseClient: SupabaseClient): List<Usuario> {

            val listaUsuarios = supabaseClient.postgrest["usuarios"].select().decodeList<Usuario>()

            return listaUsuarios
        }
    }
}