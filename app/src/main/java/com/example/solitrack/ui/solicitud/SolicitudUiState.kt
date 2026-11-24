package com.example.solitrack.ui.solicitud

import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.data.model.Servicio

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
    val guardadoExitosamente: Boolean = false,

    // Catálogo de servicios que llega desde la API REST
    val servicios: List<Servicio> = emptyList(),
    val cargandoServicios: Boolean = false,
    val errorServicios: String? = null
) {
    // Sugerencias para el campo Servicio: las de la categoría elegida
    // (o todas si todavía no se elige categoría)
    val serviciosSugeridos: List<Servicio>
        get() = servicios.filter { categoria.isBlank() || it.categoria == categoria }

    val esEdicion: Boolean
        get() = id != null

    val tieneErrores: Boolean
        get() = listOf(tituloError, descripcionError, categoriaError, servicioError)
            .any { it != null }
}
