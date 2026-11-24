package com.example.solitrack.data.remote

import com.example.solitrack.data.model.Servicio
import retrofit2.http.GET

// Esta interfaz define los endpoints HTTP de la API.
// Retrofit escribe la implementación al hacer .create(ApiService::class.java)
interface ApiService {

    // GET <baseUrl>/servicios.json -> lista de servicios del catálogo
    @GET("servicios.json")
    suspend fun obtenerServicios(): List<Servicio>
}
