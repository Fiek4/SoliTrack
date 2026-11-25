package com.example.solitrack.data.model

// Prioridad que define el administrador
enum class Prioridad(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    companion object {
        fun desdeEtiqueta(etiqueta: String?): Prioridad? = entries.find { it.etiqueta == etiqueta }
    }
}
