package com.example.solitrack.data.repository

import com.example.solitrack.data.local.UsuarioDao
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.Rol

// Valida el login contra la tabla "usuarios" de Room y devuelve el usuario con su rol
class AuthRepository(private val dao: UsuarioDao) {

    /**
     * Retorna el usuario si las credenciales son correctas, o null si no.
     */
    suspend fun login(usuario: String, clave: String): UsuarioEntity? {
        // Se asegura de que existan las cuentas de prueba (no duplica: OnConflict IGNORE)
        dao.insertarTodos(USUARIOS_PRUEBA)
        return dao.buscarPorCredenciales(usuario, clave)
    }

    suspend fun obtenerTecnicos(): List<UsuarioEntity> = dao.obtenerPorRol(Rol.TECNICO)

    companion object {
        const val CLAVE_PRUEBA = "1234"

        // Una cuenta ficticia por perfil (+ un segundo técnico para poder asignar)
        val USUARIOS_PRUEBA = listOf(
            UsuarioEntity(usuario = "cliente", clave = CLAVE_PRUEBA, nombre = "Camila Rojas", rol = Rol.CLIENTE),
            UsuarioEntity(usuario = "admin", clave = CLAVE_PRUEBA, nombre = "Andrés Soto", rol = Rol.ADMINISTRADOR),
            UsuarioEntity(usuario = "tecnico", clave = CLAVE_PRUEBA, nombre = "Tomás Díaz", rol = Rol.TECNICO),
            UsuarioEntity(usuario = "tecnico2", clave = CLAVE_PRUEBA, nombre = "Valentina Muñoz", rol = Rol.TECNICO)
        )
    }
}
