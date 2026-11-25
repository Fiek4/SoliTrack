package com.example.solitrack.data.model

// Perfiles del caso 009D. Room guarda el nombre del enum (ej: "TECNICO") como texto.
enum class Rol(val etiqueta: String) {
    CLIENTE("Cliente"),
    ADMINISTRADOR("Administrador"),
    TECNICO("Técnico")
}
