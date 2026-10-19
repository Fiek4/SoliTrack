package com.example.solitrack.data.model

enum class EstadoSolicitud(val etiqueta: String) {
    RECIBIDA("Recibida"),
    EN_REVISION("En revisión"),
    PENDIENTE_INFORMACION("Pendiente de información"),
    ASIGNADA("Asignada"),
    EN_PROCESO("En proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada"),
    CANCELADA("Cancelada");

    companion object {
        fun desdeEtiqueta(etiqueta: String): EstadoSolicitud =
            entries.find { it.etiqueta == etiqueta } ?: RECIBIDA
    }
}
