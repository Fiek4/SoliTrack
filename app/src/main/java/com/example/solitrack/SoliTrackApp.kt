package com.example.solitrack

import android.app.Application
import com.example.solitrack.data.local.AppDatabase
import com.example.solitrack.data.repository.SolicitudRepository
import com.example.solitrack.ui.components.NotificacionUtils

class SoliTrackApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // El canal de notificaciones se crea una sola vez al abrir la app
        NotificacionUtils.crearCanal(this)
    }

    // "by lazy" = se crea la primera vez que se usa, no antes
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val solicitudRepository: SolicitudRepository by lazy {
        SolicitudRepository(database.solicitudDao())
    }
}
