package com.example.solitrack.data.model

import com.google.gson.annotations.SerializedName

// Un servicio del catálogo que entrega la API REST.
// Gson convierte cada objeto del JSON en un Servicio.
data class Servicio(
    val id: Int,
    @SerializedName("servicio") val nombre: String,
    val categoria: String,
    val descripcion: String
)
