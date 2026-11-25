package com.example.solitrack.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Avances y comentarios de una solicitud.
// interno = true -> solo lo ven el administrador y los técnicos (el cliente no).
@Entity(tableName = "comentarios", indices = [Index("solicitudId")])
data class ComentarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val solicitudId: Long,
    val autorNombre: String,
    val autorRol: String,
    val texto: String,
    val interno: Boolean,
    val fecha: Long
)
