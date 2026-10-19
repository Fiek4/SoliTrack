package com.example.solitrack.data.model


enum class Categoria(val etiqueta: String, val requiereServicio: Boolean) {
    DESARROLLO_WEB("Desarrollo web", true),
    MANTENIMIENTO_WEB("Mantenimiento web", true),
    INCIDENTE_TECNICO("Incidente técnico", true),
    HOSTING("Hosting", true),
    SOPORTE_CORREO("Soporte de correo", true),
    MARKETING_DIGITAL("Marketing digital", true),
    SOLICITUD_CAMBIO("Solicitud de cambio", true),
    CONSULTA("Consulta", false),
    ACCESO_CONFIGURACION("Acceso o configuración", true),
    OTRO("Otro requerimiento", false);

    companion object {
        fun desdeEtiqueta(etiqueta: String): Categoria? =
            entries.find { it.etiqueta == etiqueta }
    }
}
