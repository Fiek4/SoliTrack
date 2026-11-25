package com.example.solitrack.data.local

import androidx.room.Dao
import androidx.room.Delete
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

    // Cliente: solo las solicitudes que él creó
    @Query("SELECT * FROM solicitudes WHERE clienteId = :clienteId ORDER BY fechaCreacion DESC")
    fun obtenerPorCliente(clienteId: Long): Flow<List<SolicitudEntity>>

    // Técnico: solo las que tiene asignadas
    @Query("SELECT * FROM solicitudes WHERE tecnicoId = :tecnicoId ORDER BY fechaCreacion DESC")
    fun obtenerPorTecnico(tecnicoId: Long): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    // Retorna el id generado para la nueva fila
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)

    // Room busca la fila por su clave primaria (id) y la borra
    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity)

    @Query("UPDATE solicitudes SET codigo = :codigo WHERE id = :id")
    suspend fun asignarCodigo(id: Long, codigo: String)
}
