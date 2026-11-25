package com.example.solitrack.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.example.solitrack.data.local.SolicitudEntity

// WhatsApp simulado (caso 009D): no hay integración real con WhatsApp Business,
// solo se abre WhatsApp con un mensaje ya armado que el cliente decide enviar.
object CompartirUtils {

    private const val PAQUETE_WHATSAPP = "com.whatsapp"

    fun mensajeReferencia(solicitud: SolicitudEntity): String = buildString {
        appendLine("Hola, registré una solicitud en SoliTrack.")
        appendLine("Código de seguimiento: ${solicitud.codigo}")
        appendLine("Título: ${solicitud.titulo}")
        appendLine("Categoría: ${solicitud.categoria}")
        append("Estado actual: ${solicitud.estado}")
    }

    fun compartirPorWhatsApp(context: Context, solicitud: SolicitudEntity) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensajeReferencia(solicitud))
            setPackage(PAQUETE_WHATSAPP)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Sin WhatsApp instalado (ej: emulador): se ofrece cualquier app para compartir
            intent.setPackage(null)
            context.startActivity(Intent.createChooser(intent, "Compartir solicitud"))
        }
    }
}
