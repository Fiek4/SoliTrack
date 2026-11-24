package com.example.solitrack.data.repository

import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.remote.ApiService

// Accede al catálogo de servicios usando Retrofit.
// Recibe el ApiService por constructor para poder reemplazarlo por un mock en los tests.
class ServicioRepository(private val api: ApiService) {

    suspend fun obtenerServicios(): List<Servicio> = api.obtenerServicios()
}
