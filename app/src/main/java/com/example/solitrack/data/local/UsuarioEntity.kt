package com.example.solitrack.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.solitrack.data.model.Rol

// Tabla de usuarios. El índice único impide dos cuentas con el mismo nombre de usuario.
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["usuario"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val usuario: String,

    // Contraseña ficticia de prueba (en una app real se guardaría cifrada)
    val clave: String,

    val nombre: String,
    val rol: Rol
)
