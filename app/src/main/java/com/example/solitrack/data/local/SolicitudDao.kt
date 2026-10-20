package com.example.solitrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    // Devuelve un Flow: cada vez que la tabla cambia (insert/update),
    // Room emite la lista nueva AUTOMÁTICAMENTE -> la pantalla se actualiza sola.
    @Query("SELECT * FROM solicitudes ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    // Retorna el id generado para la nueva fila
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)

    @Query("UPDATE solicitudes SET codigo = :codigo WHERE id = :id")
    suspend fun asignarCodigo(id: Long, codigo: String)
}
