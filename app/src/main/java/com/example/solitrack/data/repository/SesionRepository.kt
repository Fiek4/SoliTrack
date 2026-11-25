package com.example.solitrack.data.repository

import com.example.solitrack.data.local.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Guarda en memoria quién inició sesión. Vive en SoliTrackApp (una sola instancia),
// así todas las pantallas saben el usuario y su rol sin pasarlo por cada ruta.
class SesionRepository {

    // null = nadie ha iniciado sesión
    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario.asStateFlow()

    fun iniciar(usuario: UsuarioEntity) {
        _usuario.value = usuario
    }

    fun cerrar() {
        _usuario.value = null
    }
}
