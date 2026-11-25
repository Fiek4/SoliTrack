package com.example.solitrack.data.model

import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.local.UsuarioEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

// Reglas del flujo de atención y de permisos por perfil (caso 009D)
class FlujoEstadosTest : StringSpec({

    val cliente = UsuarioEntity(id = 1, usuario = "cliente", clave = "1234", nombre = "Camila", rol = Rol.CLIENTE)
    val otroCliente = UsuarioEntity(id = 5, usuario = "otro", clave = "1234", nombre = "Otro", rol = Rol.CLIENTE)
    val admin = UsuarioEntity(id = 2, usuario = "admin", clave = "1234", nombre = "Andrés", rol = Rol.ADMINISTRADOR)
    val tecnico = UsuarioEntity(id = 3, usuario = "tecnico", clave = "1234", nombre = "Tomás", rol = Rol.TECNICO)
    val tecnico2 = UsuarioEntity(id = 4, usuario = "tecnico2", clave = "1234", nombre = "Valentina", rol = Rol.TECNICO)

    fun solicitud(estado: EstadoSolicitud, tecnicoId: Long? = null) = SolicitudEntity(
        id = 1, codigo = "SOL-0001", titulo = "Sitio caído", descripcion = "No carga",
        categoria = "Hosting", servicio = "Hosting básico", clienteId = 1, tecnicoId = tecnicoId,
        estado = estado.etiqueta, fechaCreacion = 0, fechaActualizacion = 0
    )

    fun destinos(usuario: UsuarioEntity, s: SolicitudEntity) =
        FlujoEstados.accionesDisponibles(usuario, s).map { it.destino }

    // ---------- Administrador ----------

    "el admin inicia la revisión de una solicitud recibida" {
        destinos(admin, solicitud(EstadoSolicitud.RECIBIDA)) shouldContain EstadoSolicitud.EN_REVISION
    }

    "el admin puede cancelar mientras no esté cerrada, y cancelar exige motivo" {
        val cancelar = FlujoEstados.accionesDisponibles(admin, solicitud(EstadoSolicitud.EN_PROCESO, 3))
            .first { it.destino == EstadoSolicitud.CANCELADA }
        cancelar.requiereComentario.shouldBeTrue()
        destinos(admin, solicitud(EstadoSolicitud.CERRADA)).shouldBeEmpty()
    }

    "solo el admin cierra, y solo cuando está resuelta" {
        destinos(admin, solicitud(EstadoSolicitud.RESUELTA, 3)) shouldContain EstadoSolicitud.CERRADA
        destinos(tecnico, solicitud(EstadoSolicitud.RESUELTA, 3)) shouldNotContain EstadoSolicitud.CERRADA
        destinos(cliente, solicitud(EstadoSolicitud.RESUELTA, 3)) shouldNotContain EstadoSolicitud.CERRADA
    }

    "solo el admin asigna y prioriza" {
        FlujoEstados.puedeAsignar(admin, solicitud(EstadoSolicitud.EN_REVISION)).shouldBeTrue()
        FlujoEstados.puedeAsignar(tecnico, solicitud(EstadoSolicitud.EN_REVISION)).shouldBeFalse()
        FlujoEstados.puedePriorizar(cliente, solicitud(EstadoSolicitud.RECIBIDA)).shouldBeFalse()
    }

    // ---------- Técnico ----------

    "el técnico asignado inicia el trabajo y luego la marca resuelta" {
        destinos(tecnico, solicitud(EstadoSolicitud.ASIGNADA, 3)) shouldBe listOf(EstadoSolicitud.EN_PROCESO)
        destinos(tecnico, solicitud(EstadoSolicitud.EN_PROCESO, 3)) shouldContain EstadoSolicitud.RESUELTA
    }

    "un técnico no puede tocar solicitudes asignadas a otro técnico" {
        destinos(tecnico2, solicitud(EstadoSolicitud.ASIGNADA, 3)).shouldBeEmpty()
    }

    // ---------- Cliente ----------

    "el cliente responde cuando le piden información y vuelve al técnico asignado" {
        destinos(cliente, solicitud(EstadoSolicitud.PENDIENTE_INFORMACION, 3)) shouldBe
            listOf(EstadoSolicitud.EN_PROCESO)
    }

    "el cliente puede pedir reabrir una solicitud resuelta" {
        destinos(cliente, solicitud(EstadoSolicitud.RESUELTA, 3)) shouldBe listOf(EstadoSolicitud.EN_PROCESO)
    }

    "un cliente no tiene acciones sobre solicitudes de otro cliente" {
        destinos(otroCliente, solicitud(EstadoSolicitud.RESUELTA, 3)).shouldBeEmpty()
    }

    "el cliente solo edita sus datos mientras está Recibida" {
        FlujoEstados.puedeEditarDatos(cliente, solicitud(EstadoSolicitud.RECIBIDA)).shouldBeTrue()
        FlujoEstados.puedeEditarDatos(cliente, solicitud(EstadoSolicitud.EN_REVISION)).shouldBeFalse()
        FlujoEstados.puedeEditarDatos(tecnico, solicitud(EstadoSolicitud.RECIBIDA)).shouldBeFalse()
    }

    "el cliente no ve comentarios internos" {
        FlujoEstados.puedeVerInternos(cliente).shouldBeFalse()
        FlujoEstados.puedeVerInternos(tecnico).shouldBeTrue()
    }
})
