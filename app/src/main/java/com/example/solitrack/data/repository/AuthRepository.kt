package com.example.solitrack.data.repository

class AuthRepository {

    fun login(usuario: String, clave: String): Boolean =
        usuario == USUARIO_DEMO && clave == CLAVE_DEMO

    companion object {
        const val USUARIO_DEMO = "cliente"
        const val CLAVE_DEMO = "1234"
    }
}
