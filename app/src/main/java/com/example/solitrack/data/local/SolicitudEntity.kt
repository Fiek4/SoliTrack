package com.example.solitrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.solitrack.data.model.EstadoSolicitud

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Código de seguimiento visible para el cliente (ej: SOL-0001).
    // Lo asigna el Repository después de insertar.
    val codigo: String = "",

    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,

    // Ruta de la foto de evidencia (null si no tiene)
    val evidenciaUri: String? = null,

    // Toda solicitud nueva comienza en "Recibida"
    val estado: String = EstadoSolicitud.RECIBIDA.etiqueta,

    // Fechas guardadas en milisegundos (System.currentTimeMillis())
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)
