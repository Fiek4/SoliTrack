package com.example.solitrack.data.remote

import com.example.solitrack.data.model.Servicio
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Esta interfaz define los endpoints HTTP de la API.
// Retrofit escribe la implementación al hacer .create(ApiService::class.java)
interface ApiService {

    // GET <baseUrl>/servicios.json -> lista de servicios del catálogo
    @GET("servicios.json")
    suspend fun obtenerServicios(): List<Servicio>

    // ---------- OTROS MÉTODOS HTTP (Guía 14, punto 11) ----------
    // GitHub solo entrega archivos (GET), así que crear/editar/borrar se envía a
    // JSONPlaceholder: una API de prueba que responde como si guardara, pero no guarda nada.
    // Si la URL de la anotación es completa (https://...), Retrofit ignora la BASE_URL.

    // POST = crear. @Body: el objeto viaja como JSON en el cuerpo de la petición
    @POST("$URL_PRUEBA/posts")
    suspend fun crearServicio(@Body servicio: Servicio): Servicio

    // PUT = reemplazar/actualizar. @Path: {id} se cambia por el valor del parámetro
    @PUT("$URL_PRUEBA/posts/{id}")
    suspend fun actualizarServicio(@Path("id") id: Int, @Body servicio: Servicio): Servicio

    // DELETE = borrar. No devuelve datos, solo el código HTTP (200 = OK)
    @DELETE("$URL_PRUEBA/posts/{id}")
    suspend fun eliminarServicio(@Path("id") id: Int): Response<Unit>

    companion object {
        const val URL_PRUEBA = "https://jsonplaceholder.typicode.com"
    }
}
