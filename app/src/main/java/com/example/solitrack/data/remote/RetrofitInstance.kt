package com.example.solitrack.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton con la configuración de Retrofit
object RetrofitInstance {

    // API de prueba: el catálogo vive en api/servicios.json de nuestro repositorio
    // y GitHub lo entrega como JSON por HTTPS
    private const val BASE_URL = "https://raw.githubusercontent.com/Fiek4/SoliTrack/main/api/"

    // Se instancia el servicio de la API una sola vez
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                                  // URL base de la API
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON -> objetos Kotlin
            .build()
            .create(ApiService::class.java)                     // Implementa la interfaz ApiService
    }
}
