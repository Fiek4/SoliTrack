package com.example.solitrack.data.repository

import com.example.solitrack.data.local.SolicitudDao
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.model.EstadoSolicitud
import java.util.Locale
import kotlinx.coroutines.flow.Flow

class SolicitudRepository(private val dao: SolicitudDao) {

    fun obtenerTodas(): Flow<List<SolicitudEntity>> = dao.obtenerTodas()

    suspend fun obtenerPorId(id: Long): SolicitudEntity? = dao.obtenerPorId(id)

    /**
     * Registra una solicitud nueva y retorna su código de seguimiento.
     */
    suspend fun crear(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String?
    ): String {
        val ahora = System.currentTimeMillis()

        val nueva = SolicitudEntity(
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            servicio = servicio,
            evidenciaUri = evidenciaUri,
            estado = EstadoSolicitud.RECIBIDA.etiqueta,
            fechaCreacion = ahora,
            fechaActualizacion = ahora
        )

        // El id lo genera Room (autoincremental), por eso el código es único
        val id = dao.insertar(nueva)
        val codigo = generarCodigo(id)
        dao.asignarCodigo(id, codigo)
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

    companion object {
        // 1 -> "SOL-0001", 25 -> "SOL-0025"
        fun generarCodigo(id: Long): String = String.format(Locale.ROOT, "SOL-%04d", id)
    }
}
