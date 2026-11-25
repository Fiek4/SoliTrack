package com.example.solitrack.ui.catalogo

import com.example.solitrack.data.model.Servicio

data class CatalogoUiState(
    val servicios: List<Servicio> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,

    // Diálogo crear/editar: null = cerrado. Con id == 0 es un servicio nuevo
    val enEdicion: Servicio? = null,
    val nombreError: String? = null,
    val categoriaError: String? = null,
    val enviando: Boolean = false,

    // Mensaje corto con la respuesta del servidor (se muestra en un Snackbar)
    val mensaje: String? = null
)
