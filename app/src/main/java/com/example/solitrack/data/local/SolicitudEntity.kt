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

    // Cliente que creó la solicitud (id de la tabla usuarios)
    val clienteId: Long = 0,

    // Técnico responsable; null mientras el administrador no la asigne
    val tecnicoId: Long? = null,

    // Prioridad que define el administrador (Baja, Media, Alta); null = sin definir
    val prioridad: String? = null,

    // Fechas guardadas en milisegundos (System.currentTimeMillis())
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)
