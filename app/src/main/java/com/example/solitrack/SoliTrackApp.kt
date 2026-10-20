package com.example.solitrack

import android.app.Application
import com.example.solitrack.data.local.AppDatabase
import com.example.solitrack.data.repository.SolicitudRepository

class SoliTrackApp : Application() {

    // "by lazy" = se crea la primera vez que se usa, no antes
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val solicitudRepository: SolicitudRepository by lazy {
        SolicitudRepository(database.solicitudDao())
    }
}
