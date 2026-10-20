package com.example.solitrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.ui.theme.EstadoAsignada
import com.example.solitrack.ui.theme.EstadoCancelada
import com.example.solitrack.ui.theme.EstadoCerrada
import com.example.solitrack.ui.theme.EstadoEnProceso
import com.example.solitrack.ui.theme.EstadoEnRevision
import com.example.solitrack.ui.theme.EstadoPendienteInfo
import com.example.solitrack.ui.theme.EstadoRecibida
import com.example.solitrack.ui.theme.EstadoResuelta

@Composable
fun EstadoChip(
    estado: String,
    modifier: Modifier = Modifier
) {
    val color = colorDeEstado(estado)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = estado,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun colorDeEstado(estado: String): Color = when (EstadoSolicitud.desdeEtiqueta(estado)) {
    EstadoSolicitud.RECIBIDA -> EstadoRecibida
    EstadoSolicitud.EN_REVISION -> EstadoEnRevision
    EstadoSolicitud.PENDIENTE_INFORMACION -> EstadoPendienteInfo
    EstadoSolicitud.ASIGNADA -> EstadoAsignada
    EstadoSolicitud.EN_PROCESO -> EstadoEnProceso
    EstadoSolicitud.RESUELTA -> EstadoResuelta
    EstadoSolicitud.CERRADA -> EstadoCerrada
    EstadoSolicitud.CANCELADA -> EstadoCancelada
}
