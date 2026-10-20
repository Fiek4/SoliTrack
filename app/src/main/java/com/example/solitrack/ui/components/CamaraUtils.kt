package com.example.solitrack.ui.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CamaraUtils {

    fun crearUriParaFoto(context: Context): Uri {
        val carpeta = File(context.filesDir, "evidencias").apply {
            if (!exists()) mkdirs()
        }
        val archivo = File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }
}
