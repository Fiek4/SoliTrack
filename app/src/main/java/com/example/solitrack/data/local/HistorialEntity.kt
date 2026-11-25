package com.example.solitrack.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Trazabilidad (caso 009D): cada acción sobre una solicitud queda registrada
// con el estado anterior, el nuevo, quién lo hizo y cuándo.
@Entity(tableName = "historial", indices = [Index("solicitudId")])
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val solicitudId: Long,

    // null cuando la solicitud recién se crea
    val estadoAnterior: String?,
    val estadoNuevo: String,

    // Descripción de lo que pasó (ej: "Asignada a Tomás Díaz")
    val accion: String,

    val usuarioNombre: String,
    val usuarioRol: String,

    val fecha: Long
)
