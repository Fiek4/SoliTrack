package com.example.solitrack.ui.resumen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.data.model.Prioridad
import com.example.solitrack.ui.components.colorDeEstado
import com.example.solitrack.ui.solicitud.SolicitudViewModel

// Panel del administrador: totales por estado y por prioridad (caso 009D, "resumen por estado")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenScreen(
    onVolver: () -> Unit,
    viewModel: SolicitudViewModel = viewModel(factory = SolicitudViewModel.Factory)
) {
    // El administrador recibe todas las solicitudes
    val solicitudes by viewModel.solicitudes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        val lista = solicitudes
        if (lista == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val resumen = calcularResumen(lista)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador("Total", resumen.total, Modifier.weight(1f))
                    Indicador("Abiertas", resumen.abiertas, Modifier.weight(1f))
                    Indicador("Sin asignar", resumen.sinAsignar, Modifier.weight(1f))
                }
            }
            item {
                Tarjeta("Por estado") {
                    EstadoSolicitud.entries.forEach { estado ->
                        Barra(
                            etiqueta = estado.etiqueta,
                            cantidad = resumen.porEstado[estado] ?: 0,
                            total = resumen.total,
                            color = colorDeEstado(estado.etiqueta)
                        )
                    }
                }
            }
            item {
                Tarjeta("Por prioridad") {
                    Prioridad.entries.forEach { prioridad ->
                        Barra(
                            etiqueta = prioridad.etiqueta,
                            cantidad = resumen.porPrioridad[prioridad] ?: 0,
                            total = resumen.total,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Barra(
                        etiqueta = "Sin definir",
                        cantidad = resumen.sinPrioridad,
                        total = resumen.total,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/** Cálculo puro (sin Compose) para poder probarlo con tests. */
data class Resumen(
    val total: Int,
    val abiertas: Int,
    val sinAsignar: Int,
    val porEstado: Map<EstadoSolicitud, Int>,
    val porPrioridad: Map<Prioridad, Int>,
    val sinPrioridad: Int
)

fun calcularResumen(lista: List<SolicitudEntity>): Resumen {
    val finales = setOf(EstadoSolicitud.CERRADA, EstadoSolicitud.CANCELADA)
    val estados = lista.map { EstadoSolicitud.desdeEtiqueta(it.estado) }
    return Resumen(
        total = lista.size,
        abiertas = estados.count { it !in finales },
        sinAsignar = lista.count { it.tecnicoId == null && EstadoSolicitud.desdeEtiqueta(it.estado) !in finales },
        porEstado = estados.groupingBy { it }.eachCount(),
        porPrioridad = lista.mapNotNull { Prioridad.desdeEtiqueta(it.prioridad) }.groupingBy { it }.eachCount(),
        sinPrioridad = lista.count { it.prioridad == null }
    )
}

@Composable
private fun Indicador(titulo: String, valor: Int, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$valor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
private fun Barra(etiqueta: String, cantidad: Int, total: Int, color: Color) {
    val fraccion = if (total == 0) 0f else cantidad.toFloat() / total
    Column {
        Row {
            Text(etiqueta, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("$cantidad", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraccion)
                    .background(color)
            )
        }
    }
}
