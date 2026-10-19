package com.example.solitrack.ui.login

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,

    val usuarioError: String? = null,
    val claveError: String? = null,
    val errorGeneral: String? = null,

    val cargando: Boolean = false,
    val loginExitoso: Boolean = false
)
