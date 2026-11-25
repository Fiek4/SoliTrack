package com.example.solitrack.data.repository

import com.example.solitrack.data.local.ComentarioEntity
import com.example.solitrack.data.local.HistorialEntity
import com.example.solitrack.data.local.SeguimientoDao
import com.example.solitrack.data.local.SolicitudDao
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.data.model.Prioridad
import java.util.Locale
import kotlinx.coroutines.flow.Flow

class SolicitudRepository(
    private val dao: SolicitudDao,
    private val seguimientoDao: SeguimientoDao
) {

    fun obtenerTodas(): Flow<List<SolicitudEntity>> = dao.obtenerTodas()

    fun obtenerPorCliente(clienteId: Long): Flow<List<SolicitudEntity>> =
        dao.obtenerPorCliente(clienteId)

    fun obtenerPorTecnico(tecnicoId: Long): Flow<List<SolicitudEntity>> =
        dao.obtenerPorTecnico(tecnicoId)

    suspend fun obtenerPorId(id: Long): SolicitudEntity? = dao.obtenerPorId(id)

    // Versión reactiva para la pantalla de detalle: se actualiza sola al cambiar el estado
    fun observarPorId(id: Long): Flow<SolicitudEntity?> = dao.observarPorId(id)

    fun observarHistorial(id: Long): Flow<List<HistorialEntity>> = seguimientoDao.obtenerHistorial(id)

    fun observarComentarios(id: Long): Flow<List<ComentarioEntity>> = seguimientoDao.obtenerComentarios(id)

    /**
     * Registra una solicitud nueva y retorna su código de seguimiento.
     * También deja el primer registro en el historial.
     */
    suspend fun crear(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String?,
        cliente: UsuarioEntity
    ): String {
        val ahora = System.currentTimeMillis()

        val nueva = SolicitudEntity(
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            servicio = servicio,
            evidenciaUri = evidenciaUri,
            clienteId = cliente.id,
            estado = EstadoSolicitud.RECIBIDA.etiqueta,
            fechaCreacion = ahora,
            fechaActualizacion = ahora
        )

        // El id lo genera Room (autoincremental), por eso el código es único
        val id = dao.insertar(nueva)
        val codigo = generarCodigo(id)
        dao.asignarCodigo(id, codigo)

        registrar(id, null, EstadoSolicitud.RECIBIDA.etiqueta, "Solicitud registrada", cliente, ahora)
        return codigo
    }

    /**
     * Modifica los datos básicos. El código, el estado y la fecha de
     * creación NO cambian; solo se actualiza la fecha de actualización.
     */
    suspend fun actualizar(
        id: Long,
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String?
    ) {
        val actual = dao.obtenerPorId(id) ?: return

        dao.actualizar(
            actual.copy(
                titulo = titulo,
                descripcion = descripcion,
                categoria = categoria,
                servicio = servicio,
                evidenciaUri = evidenciaUri,
                fechaActualizacion = System.currentTimeMillis()
            )
        )
    }

    /**
     * Cambia el estado y lo deja en el historial (estado anterior -> nuevo, usuario, fecha).
     * Si viene un comentario (motivo, información pedida, etc.) se guarda visible para el cliente.
     */
    suspend fun cambiarEstado(
        id: Long,
        nuevo: EstadoSolicitud,
        accion: String,
        usuario: UsuarioEntity,
        comentario: String? = null
    ) {
        val actual = dao.obtenerPorId(id) ?: return
        val ahora = System.currentTimeMillis()

        dao.actualizar(actual.copy(estado = nuevo.etiqueta, fechaActualizacion = ahora))
        registrar(id, actual.estado, nuevo.etiqueta, accion, usuario, ahora)

        if (!comentario.isNullOrBlank()) {
            agregarComentario(id, usuario, comentario, interno = false)
        }
    }

    /** El administrador asigna (o reasigna) un técnico: la solicitud pasa a "Asignada". */
    suspend fun asignarTecnico(id: Long, tecnico: UsuarioEntity, admin: UsuarioEntity) {
        val actual = dao.obtenerPorId(id) ?: return
        val ahora = System.currentTimeMillis()
        val accion = if (actual.tecnicoId == null) "Asignada a ${tecnico.nombre}" else "Reasignada a ${tecnico.nombre}"

        dao.actualizar(
            actual.copy(
                tecnicoId = tecnico.id,
                estado = EstadoSolicitud.ASIGNADA.etiqueta,
                fechaActualizacion = ahora
            )
        )
        registrar(id, actual.estado, EstadoSolicitud.ASIGNADA.etiqueta, accion, admin, ahora)
    }

    /** Define la prioridad. El estado no cambia, pero la acción queda en el historial. */
    suspend fun definirPrioridad(id: Long, prioridad: Prioridad, admin: UsuarioEntity) {
        val actual = dao.obtenerPorId(id) ?: return
        if (actual.prioridad == prioridad.etiqueta) return
        val ahora = System.currentTimeMillis()

        dao.actualizar(actual.copy(prioridad = prioridad.etiqueta, fechaActualizacion = ahora))
        registrar(id, actual.estado, actual.estado, "Prioridad: ${prioridad.etiqueta}", admin, ahora)
    }

    suspend fun agregarComentario(id: Long, usuario: UsuarioEntity, texto: String, interno: Boolean) {
        seguimientoDao.insertarComentario(
            ComentarioEntity(
                solicitudId = id,
                autorNombre = usuario.nombre,
                autorRol = usuario.rol.etiqueta,
                texto = texto.trim(),
                interno = interno,
                fecha = System.currentTimeMillis()
            )
        )
    }

    /**
     * Elimina la solicitud junto con su historial y comentarios.
     * La lista se actualiza sola porque Room vuelve a emitir el Flow.
     */
    suspend fun eliminar(id: Long) {
        val actual = dao.obtenerPorId(id) ?: return
        seguimientoDao.eliminarHistorial(id)
        seguimientoDao.eliminarComentarios(id)
        dao.eliminar(actual)
    }

    private suspend fun registrar(
        id: Long,
        anterior: String?,
        nuevo: String,
        accion: String,
        usuario: UsuarioEntity,
        fecha: Long
    ) {
        seguimientoDao.insertarHistorial(
            HistorialEntity(
                solicitudId = id,
                estadoAnterior = anterior,
                estadoNuevo = nuevo,
                accion = accion,
                usuarioNombre = usuario.nombre,
                usuarioRol = usuario.rol.etiqueta,
                fecha = fecha
            )
        )
    }

    companion object {
        // 1 -> "SOL-0001", 25 -> "SOL-0025"
        fun generarCodigo(id: Long): String = String.format(Locale.ROOT, "SOL-%04d", id)
    }
}
