package com.example.solitrack.data.repository

import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.remote.ApiService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

// Prueba el repositorio SIN internet: el ApiService se reemplaza por un mock (MockK)
// que responde lo que nosotros le indicamos.
class ServicioRepositoryTest : StringSpec({

    val hosting = Servicio(1, "Hosting básico", "Hosting", "Alojamiento web")
    val correo = Servicio(2, "Migración de correos", "Soporte de correo", "Traslado de buzones")

    "obtenerServicios devuelve la lista que entrega la API (GET)" {
        val api = mockk<ApiService>()
        coEvery { api.obtenerServicios() } returns listOf(hosting, correo)

        val resultado = ServicioRepository(api).obtenerServicios()

        resultado shouldBe listOf(hosting, correo)
        coVerify(exactly = 1) { api.obtenerServicios() }
    }

    "obtenerServicios propaga el error si no hay conexión" {
        val api = mockk<ApiService>()
        coEvery { api.obtenerServicios() } throws java.io.IOException("Sin internet")

        shouldThrow<java.io.IOException> {
            ServicioRepository(api).obtenerServicios()
        }
    }

    "crear envía el servicio por POST y devuelve la respuesta del servidor" {
        val api = mockk<ApiService>()
        val nuevo = hosting.copy(id = 0)
        coEvery { api.crearServicio(nuevo) } returns nuevo.copy(id = 101)

        val resultado = ServicioRepository(api).crear(nuevo)

        resultado.id shouldBe 101
        coVerify { api.crearServicio(nuevo) }
    }

    "actualizar usa el id del servicio en la ruta del PUT" {
        val api = mockk<ApiService>()
        val editado = correo.copy(nombre = "Migración de correos Pro")
        coEvery { api.actualizarServicio(2, editado) } returns editado

        ServicioRepository(api).actualizar(editado) shouldBe editado
        coVerify { api.actualizarServicio(2, editado) }
    }

    "eliminar llama al DELETE con el id correcto" {
        val api = mockk<ApiService>()
        coEvery { api.eliminarServicio(1) } returns Response.success(Unit)

        ServicioRepository(api).eliminar(1)

        coVerify(exactly = 1) { api.eliminarServicio(1) }
    }

    "eliminar lanza HttpException si el servidor responde con error" {
        val api = mockk<ApiService>()
        coEvery { api.eliminarServicio(99) } returns
            Response.error(404, "".toResponseBody(null))

        shouldThrow<HttpException> {
            ServicioRepository(api).eliminar(99)
        }
    }
})
