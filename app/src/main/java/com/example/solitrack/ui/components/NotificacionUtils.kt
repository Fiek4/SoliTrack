package com.example.solitrack.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.solitrack.MainActivity
import com.example.solitrack.R

/**
 * Notificaciones locales de SoliTrack (segundo recurso nativo).
 * Avisa al usuario cuando una solicitud se registra o se modifica,
 * como pide el caso: "recibir avisos sobre cambios relevantes".
 */
object NotificacionUtils {

    private const val CANAL_ID = "solicitudes"

    // Desde Android 8 (API 26) toda notificación debe pertenecer a un canal.
    // Se crea una sola vez al iniciar la app (desde SoliTrackApp).
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Solicitudes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos cuando se registra o modifica una solicitud"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }

    // Desde Android 13 (API 33) mostrar notificaciones requiere permiso del usuario.
    fun necesitaPermiso(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun tienePermiso(context: Context): Boolean =
        !necesitaPermiso() || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    fun notificarSolicitudGuardada(
        context: Context,
        codigo: String,
        titulo: String,
        esEdicion: Boolean
    ) {
        // Si el usuario no dio permiso, la app sigue funcionando sin avisos
        if (!tienePermiso(context)) return

        // Al tocar la notificación se abre la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val alTocar = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(
                if (esEdicion) "Solicitud $codigo actualizada" else "Solicitud $codigo registrada"
            )
            .setContentText(
                if (esEdicion) "$titulo · Cambios guardados" else "$titulo · Estado: Recibida"
            )
            .setContentIntent(alTocar)
            .setAutoCancel(true)
            .build()

        try {
            // El código como id: cada solicitud tiene su propia notificación
            NotificationManagerCompat.from(context).notify(codigo.hashCode(), notificacion)
        } catch (e: SecurityException) {
            // El permiso se revocó justo antes de notificar: no se muestra nada
        }
    }
}
