package com.example.solitrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SolicitudEntity::class,
        UsuarioEntity::class,
        HistorialEntity::class,
        ComentarioEntity::class
    ],
    // v2: tabla usuarios + dueño, técnico y prioridad en solicitudes
    // v3: tablas historial y comentarios
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao

    abstract fun usuarioDao(): UsuarioDao

    abstract fun seguimientoDao(): SeguimientoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solitrack.db"
                )
                    // Al subir de versión borra y recrea las tablas (son datos de prueba).
                    // En una app real se escribiría una Migration para no perder datos.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
