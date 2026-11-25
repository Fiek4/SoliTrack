package com.example.solitrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.solitrack.data.model.Rol

@Dao
interface UsuarioDao {

    // null si el usuario no existe o la contraseña no coincide
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario AND clave = :clave LIMIT 1")
    suspend fun buscarPorCredenciales(usuario: String, clave: String): UsuarioEntity?

    // IGNORE: si el usuario ya existe (índice único) no lo vuelve a insertar
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)

    // Para listar técnicos al asignar una solicitud
    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre")
    suspend fun obtenerPorRol(rol: Rol): List<UsuarioEntity>
}
