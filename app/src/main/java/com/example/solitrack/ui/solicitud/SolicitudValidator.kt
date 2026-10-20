package com.example.solitrack.ui.solicitud

import com.example.solitrack.data.model.Categoria

object SolicitudValidator {

    const val MIN_TITULO = 5
    const val MIN_DESCRIPCION = 10

    fun validarTitulo(titulo: String): String? = when {
        titulo.isBlank() -> "El título es obligatorio"
        titulo.trim().length < MIN_TITULO -> "El título debe tener al menos $MIN_TITULO caracteres"
        else -> null
    }

    fun validarDescripcion(descripcion: String): String? = when {
        descripcion.isBlank() -> "La descripción es obligatoria"
        descripcion.trim().length < MIN_DESCRIPCION -> "Describe la necesidad con al menos $MIN_DESCRIPCION caracteres"
        else -> null
    }

    fun validarCategoria(categoria: String): String? =
        if (Categoria.desdeEtiqueta(categoria) == null) "Selecciona una categoría" else null

    // "Servicio relacionado: obligatorio cuando corresponda"
    fun validarServicio(categoria: String, servicio: String): String? {
        val requiereServicio = Categoria.desdeEtiqueta(categoria)?.requiereServicio == true
        return if (requiereServicio && servicio.isBlank()) {
            "Indica el servicio o sitio relacionado"
        } else {
            null
        }
    }
}
