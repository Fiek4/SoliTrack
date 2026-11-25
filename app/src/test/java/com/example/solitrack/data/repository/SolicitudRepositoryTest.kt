package com.example.solitrack.data.repository

import com.example.solitrack.data.local.ComentarioEntity
import com.example.solitrack.data.local.HistorialEntity
import com.example.solitrack.data.local.SeguimientoDao
import com.example.solitrack.data.local.SolicitudDao
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.data.model.Prioridad
import com.example.solitrack.data.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot

// Trazabilidad: cada cambio debe quedar en el historial con estado anterior, nuevo y usuario
class SolicitudRepositoryTest : StringSpec({

    val admin = UsuarioEntity(id = 2, usuario = "admin", clave = "1234", nombre = "Andrés Soto", rol = Rol.ADMINISTRADOR)
    val tecnico = UsuarioEntity(id = 3, usuario = "tecnico", clave = "1234", nombre = "Tomás Díaz", rol = Rol.TECNICO)

    val recibida = SolicitudEntity(
        id = 1, codigo = "SOL-0001", titulo = "Sitio caído", descripcion = "No carga",
        categoria = "Hosting", servicio = "Hosting básico", clienteId = 1,
        estado = EstadoSolicitud.RECIBIDA.etiqueta, fechaCreacion = 0, fechaActualizacion = 0
    )

    fun daos(): Pair<SolicitudDao, SeguimientoDao> {
        val dao = mockk<SolicitudDao>(relaxUnitFun = true)
        coEvery { dao.obtenerPorId(1) } returns recibida
        return dao to mockk(relaxUnitFun = true)
    }

    "crear registra la solicitud y deja el primer registro en el historial" {
        val (dao, seguimiento) = daos()
        coEvery { dao.insertar(any()) } returns 7
        val cliente = UsuarioEntity(id = 1, usuario = "cliente", clave = "1234", nombre = "Camila", rol = Rol.CLIENTE)
        val historial = slot<HistorialEntity>()

        val codigo = SolicitudRepository(dao, seguimiento)
            .crear("Sitio caído", "No carga", "Hosting", "Hosting básico", null, cliente)

        codigo shouldBe "SOL-0007"
        coVerify { seguimiento.insertarHistorial(capture(historial)) }
        historial.captured.estadoNuevo shouldBe "Recibida"
        historial.captured.usuarioNombre shouldBe "Camila"
    }

    "cambiarEstado actualiza la solicitud y guarda estado anterior y nuevo" {
        val (dao, seguimiento) = daos()
        val actualizada = slot<SolicitudEntity>()
        val historial = slot<HistorialEntity>()

        SolicitudRepository(dao, seguimiento)
            .cambiarEstado(1, EstadoSolicitud.EN_REVISION, "Iniciar revisión", admin)

        coVerify { dao.actualizar(capture(actualizada)) }
        coVerify { seguimiento.insertarHistorial(capture(historial)) }
        actualizada.captured.estado shouldBe "En revisión"
        historial.captured.estadoAnterior shouldBe "Recibida"
        historial.captured.estadoNuevo shouldBe "En revisión"
        historial.captured.usuarioRol shouldBe "Administrador"
    }

    "un cambio de estado con motivo guarda también un comentario visible" {
        val (dao, seguimiento) = daos()
        val comentario = slot<ComentarioEntity>()

        SolicitudRepository(dao, seguimiento)
            .cambiarEstado(1, EstadoSolicitud.CANCELADA, "Cancelar solicitud", admin, "Duplicada")

        coVerify { seguimiento.insertarComentario(capture(comentario)) }
        comentario.captured.texto shouldBe "Duplicada"
        comentario.captured.interno shouldBe false
    }

    "asignar técnico deja la solicitud Asignada con su tecnicoId" {
        val (dao, seguimiento) = daos()
        val actualizada = slot<SolicitudEntity>()
        val historial = slot<HistorialEntity>()

        SolicitudRepository(dao, seguimiento).asignarTecnico(1, tecnico, admin)

        coVerify { dao.actualizar(capture(actualizada)) }
        coVerify { seguimiento.insertarHistorial(capture(historial)) }
        actualizada.captured.tecnicoId shouldBe 3L
        actualizada.captured.estado shouldBe "Asignada"
        historial.captured.accion shouldBe "Asignada a Tomás Díaz"
    }

    "definir prioridad no cambia el estado pero queda en el historial" {
        val (dao, seguimiento) = daos()
        val actualizada = slot<SolicitudEntity>()
        val historial = slot<HistorialEntity>()

        SolicitudRepository(dao, seguimiento).definirPrioridad(1, Prioridad.ALTA, admin)

        coVerify { dao.actualizar(capture(actualizada)) }
        coVerify { seguimiento.insertarHistorial(capture(historial)) }
        actualizada.captured.prioridad shouldBe "Alta"
        actualizada.captured.estado shouldBe "Recibida"
        historial.captured.accion shouldBe "Prioridad: Alta"
    }
})
