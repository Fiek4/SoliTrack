package com.example.solitrack.ui.solicitud

import com.example.solitrack.data.local.SolicitudEntity

// Filtros de la lista (caso 009D: búsqueda y filtros por estado y prioridad)
data class FiltrosLista(
    val busqueda: String = "",
    val estado: String? = null,
    val prioridad: String? = null
) {
    val activos: Boolean
        get() = busqueda.isNotBlank() || estado != null || prioridad != null

    fun coincide(solicitud: SolicitudEntity): Boolean {
        val texto = busqueda.trim()
        val porTexto = texto.isEmpty() ||
            solicitud.codigo.contains(texto, ignoreCase = true) ||
            solicitud.titulo.contains(texto, ignoreCase = true)
        val porEstado = estado == null || solicitud.estado == estado
        val porPrioridad = prioridad == null || solicitud.prioridad == prioridad
        return porTexto && porEstado && porPrioridad
    }
}
