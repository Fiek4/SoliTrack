package com.example.solitrack.data.repository

import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.remote.ApiService
import retrofit2.HttpException

// Accede al catálogo de servicios usando Retrofit.
// Recibe el ApiService por constructor para poder reemplazarlo por un mock en los tests.
class ServicioRepository(private val api: ApiService) {

    suspend fun obtenerServicios(): List<Servicio> = api.obtenerServicios()

    suspend fun crear(servicio: Servicio): Servicio = api.crearServicio(servicio)

    suspend fun actualizar(servicio: Servicio): Servicio =
        api.actualizarServicio(servicio.id, servicio)

    // Si el servidor responde con error (4xx/5xx) se lanza una excepción,
    // igual que con los otros métodos, para que el ViewModel la capture
    suspend fun eliminar(id: Int) {
        val respuesta = api.eliminarServicio(id)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
    }
}
