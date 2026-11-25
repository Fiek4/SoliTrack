package com.example.solitrack.data.model

import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.local.UsuarioEntity

/**
 * Un botón de cambio de estado: qué dice y a qué estado lleva.
 * requiereComentario = hay que escribir un motivo o mensaje (ej: al cancelar).
 */
data class AccionEstado(
    val etiqueta: String,
    val destino: EstadoSolicitud,
    val requiereComentario: Boolean = false
)

/**
 * Reglas del flujo de atención del caso 009D:
 * Recibida -> En revisión -> (Pendiente de información) -> Asignada -> En proceso -> Resuelta -> Cerrada
 * (Cancelada en cualquier momento antes de cerrar).
 *
 * Es un object sin Android ni Room, así se puede probar con tests unitarios simples.
 */
object FlujoEstados {

    private val FINALES = setOf(EstadoSolicitud.CERRADA, EstadoSolicitud.CANCELADA)

    fun esFinal(estado: EstadoSolicitud) = estado in FINALES

    /** Botones de cambio de estado que puede usar [usuario] sobre [solicitud]. */
    fun accionesDisponibles(usuario: UsuarioEntity, solicitud: SolicitudEntity): List<AccionEstado> {
        val estado = EstadoSolicitud.desdeEtiqueta(solicitud.estado)

        return when (usuario.rol) {
            Rol.CLIENTE -> {
                // Solo sobre sus propias solicitudes
                if (solicitud.clienteId != usuario.id) return emptyList()
                when (estado) {
                    // Responde lo que le pidieron: vuelve al técnico si tenía uno, si no a revisión
                    EstadoSolicitud.PENDIENTE_INFORMACION -> listOf(
                        AccionEstado(
                            "Enviar información",
                            if (solicitud.tecnicoId != null) EstadoSolicitud.EN_PROCESO else EstadoSolicitud.EN_REVISION,
                            requiereComentario = true
                        )
                    )
                    // Reapertura: si no quedó conforme, vuelve a proceso
                    EstadoSolicitud.RESUELTA -> listOf(
                        AccionEstado("Pedir reabrir", EstadoSolicitud.EN_PROCESO, requiereComentario = true)
                    )
                    else -> emptyList()
                }
            }

            Rol.TECNICO -> {
                // Solo sobre las que tiene asignadas, y nunca cierra definitivamente
                if (solicitud.tecnicoId != usuario.id) return emptyList()
                when (estado) {
                    EstadoSolicitud.ASIGNADA -> listOf(
                        AccionEstado("Iniciar trabajo", EstadoSolicitud.EN_PROCESO)
                    )
                    EstadoSolicitud.EN_PROCESO -> listOf(
                        AccionEstado("Marcar resuelta", EstadoSolicitud.RESUELTA, requiereComentario = true),
                        AccionEstado("Pedir información al cliente", EstadoSolicitud.PENDIENTE_INFORMACION, requiereComentario = true)
                    )
                    else -> emptyList()
                }
            }

            Rol.ADMINISTRADOR -> buildList {
                when (estado) {
                    EstadoSolicitud.RECIBIDA ->
                        add(AccionEstado("Iniciar revisión", EstadoSolicitud.EN_REVISION))
                    EstadoSolicitud.EN_REVISION ->
                        add(AccionEstado("Pedir información", EstadoSolicitud.PENDIENTE_INFORMACION, requiereComentario = true))
                    EstadoSolicitud.RESUELTA ->
                        add(AccionEstado("Cerrar solicitud", EstadoSolicitud.CERRADA))
                    else -> Unit
                }
                // Cancelar siempre pide el motivo
                if (!esFinal(estado)) {
                    add(AccionEstado("Cancelar solicitud", EstadoSolicitud.CANCELADA, requiereComentario = true))
                }
            }
        }
    }

    /** El administrador asigna o reasigna mientras la solicitud no esté resuelta ni finalizada. */
    fun puedeAsignar(usuario: UsuarioEntity, solicitud: SolicitudEntity): Boolean {
        val estado = EstadoSolicitud.desdeEtiqueta(solicitud.estado)
        return usuario.rol == Rol.ADMINISTRADOR &&
            !esFinal(estado) && estado != EstadoSolicitud.RESUELTA
    }

    /** Solo el administrador define la prioridad (antes de cerrar). */
    fun puedePriorizar(usuario: UsuarioEntity, solicitud: SolicitudEntity): Boolean =
        usuario.rol == Rol.ADMINISTRADOR && !esFinal(EstadoSolicitud.desdeEtiqueta(solicitud.estado))

    /** El cliente puede corregir sus datos solo mientras nadie la ha revisado. */
    fun puedeEditarDatos(usuario: UsuarioEntity, solicitud: SolicitudEntity): Boolean =
        usuario.rol == Rol.CLIENTE && solicitud.clienteId == usuario.id &&
            EstadoSolicitud.desdeEtiqueta(solicitud.estado) == EstadoSolicitud.RECIBIDA

    /** Los comentarios internos no los ve el cliente. */
    fun puedeVerInternos(usuario: UsuarioEntity): Boolean = usuario.rol != Rol.CLIENTE
}
