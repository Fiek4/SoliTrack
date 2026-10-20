package com.example.solitrack.ui.solicitud

import com.example.solitrack.data.model.EstadoSolicitud

data class SolicitudUiState(
    // null = solicitud nueva / con valor = editando una existente
    val id: Long? = null,
    val codigo: String = "",

    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val evidenciaUri: String? = null,
    val estado: String = EstadoSolicitud.RECIBIDA.etiqueta,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,
    val errorGeneral: String? = null,

    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
) {
    val esEdicion: Boolean
        get() = id != null

    val tieneErrores: Boolean
        get() = listOf(tituloError, descripcionError, categoriaError, servicioError)
            .any { it != null }
}
