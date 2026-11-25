package com.example.solitrack.ui.resumen

import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.data.model.Prioridad
import com.example.solitrack.ui.solicitud.FiltrosLista
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ResumenYFiltrosTest : StringSpec({

    fun solicitud(id: Long, estado: EstadoSolicitud, prioridad: String? = null, tecnicoId: Long? = null) =
        SolicitudEntity(
            id = id, codigo = "SOL-000$id", titulo = "Solicitud $id", descripcion = "-",
            categoria = "Hosting", servicio = "", estado = estado.etiqueta, prioridad = prioridad,
            tecnicoId = tecnicoId, fechaCreacion = 0, fechaActualizacion = 0
        )

    val lista = listOf(
        solicitud(1, EstadoSolicitud.RECIBIDA),
        solicitud(2, EstadoSolicitud.EN_PROCESO, "Alta", tecnicoId = 3),
        solicitud(3, EstadoSolicitud.EN_PROCESO, "Baja", tecnicoId = 3),
        solicitud(4, EstadoSolicitud.CERRADA, "Alta", tecnicoId = 4)
    )

    "el resumen cuenta totales por estado y por prioridad" {
        val resumen = calcularResumen(lista)

        resumen.total shouldBe 4
        resumen.abiertas shouldBe 3                 // la cerrada no cuenta
        resumen.sinAsignar shouldBe 1               // SOL-0001
        resumen.porEstado[EstadoSolicitud.EN_PROCESO] shouldBe 2
        resumen.porPrioridad[Prioridad.ALTA] shouldBe 2
        resumen.sinPrioridad shouldBe 1
    }

    "filtrar por estado y prioridad combina ambos filtros" {
        val filtros = FiltrosLista(estado = "En proceso", prioridad = "Alta")

        lista.filter { filtros.coincide(it) }.map { it.id } shouldBe listOf(2L)
    }

    "la búsqueda encuentra por código sin importar mayúsculas" {
        val filtros = FiltrosLista(busqueda = "sol-0003")

        lista.filter { filtros.coincide(it) }.map { it.id } shouldBe listOf(3L)
    }
})
