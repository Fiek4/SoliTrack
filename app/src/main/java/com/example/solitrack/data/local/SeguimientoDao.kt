package com.example.solitrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Historial y comentarios de las solicitudes
@Dao
interface SeguimientoDao {

    @Insert
    suspend fun insertarHistorial(historial: HistorialEntity)

    // Más reciente primero (línea de tiempo)
    @Query("SELECT * FROM historial WHERE solicitudId = :solicitudId ORDER BY fecha DESC, id DESC")
    fun obtenerHistorial(solicitudId: Long): Flow<List<HistorialEntity>>

    @Insert
    suspend fun insertarComentario(comentario: ComentarioEntity)

    @Query("SELECT * FROM comentarios WHERE solicitudId = :solicitudId ORDER BY fecha ASC, id ASC")
    fun obtenerComentarios(solicitudId: Long): Flow<List<ComentarioEntity>>

    // Al eliminar una solicitud se borra también su seguimiento
    @Query("DELETE FROM historial WHERE solicitudId = :solicitudId")
    suspend fun eliminarHistorial(solicitudId: Long)

    @Query("DELETE FROM comentarios WHERE solicitudId = :solicitudId")
    suspend fun eliminarComentarios(solicitudId: Long)
}
