package com.example.solitrack.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solitrack.data.local.ComentarioEntity
import com.example.solitrack.data.local.HistorialEntity
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.model.AccionEstado
import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.data.model.Prioridad
import com.example.solitrack.data.model.Rol
import com.example.solitrack.ui.components.CompartirUtils
import com.example.solitrack.ui.components.EstadoChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSolicitudScreen(
    solicitudId: Long,
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.factory(solicitudId))
) {
    val state by viewModel.uiState.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Acción que pide escribir un comentario antes de ejecutarse (null = diálogo cerrado)
    var accionPendiente by remember { mutableStateOf<AccionEstado?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    accionPendiente?.let { accion ->
        DialogoComentario(
            accion = accion,
            onConfirmar = { texto ->
                viewModel.ejecutarAccion(accion, texto)
                accionPendiente = null
            },
            onCancelar = { accionPendiente = null }
        )
    }

    val solicitud = state.solicitud

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(solicitud?.codigo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (solicitud != null) {
                        IconButton(onClick = { CompartirUtils.compartirPorWhatsApp(context, solicitud) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir por WhatsApp")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.cargando || solicitud == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                if (state.cargando) CircularProgressIndicator() else Text("La solicitud ya no existe")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TarjetaDatos(solicitud, state.nombreTecnico) }

            // ---------- Botones del cliente ----------
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.usuario?.rol == Rol.CLIENTE) {
                        Button(onClick = { CompartirUtils.compartirPorWhatsApp(context, solicitud) }) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Compartir por WhatsApp")
                        }
                    }
                    if (state.puedeEditar) {
                        OutlinedButton(onClick = { onEditar(solicitud.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Editar")
                        }
                    }
                }
            }

            // ---------- Gestión del administrador ----------
            if (state.puedePriorizar || state.puedeAsignar) {
                item {
                    TarjetaGestion(
                        state = state,
                        onPrioridad = viewModel::definirPrioridad,
                        onAsignar = viewModel::asignarTecnico
                    )
                }
            }

            // ---------- Cambios de estado permitidos para el rol ----------
            if (state.acciones.isNotEmpty()) {
                item {
                    Seccion("Acciones")
                    FlowRowAcciones(state.acciones) { accion ->
                        if (accion.requiereComentario) accionPendiente = accion
                        else viewModel.ejecutarAccion(accion, null)
                    }
                }
            }

            // ---------- Comentarios ----------
            item { Seccion("Comentarios y avances") }
            if (state.comentarios.isEmpty()) {
                item { TextoSecundario("Todavía no hay comentarios.") }
            }
            items(state.comentarios, key = { "c${it.id}" }) { ComentarioItem(it) }
            if (!EstadoSolicitud.desdeEtiqueta(solicitud.estado).let { it == EstadoSolicitud.CERRADA || it == EstadoSolicitud.CANCELADA }) {
                item {
                    NuevoComentario(
                        permiteInterno = state.puedeComentarInterno,
                        onEnviar = viewModel::comentar
                    )
                }
            }

            // ---------- Historial ----------
            item { Seccion("Historial") }
            items(state.historial, key = { "h${it.id}" }) { HistorialItem(it) }
        }
    }
}

@Composable
private fun TarjetaDatos(solicitud: SolicitudEntity, nombreTecnico: String?) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    solicitud.codigo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                EstadoChip(estado = solicitud.estado)
            }
            Text(solicitud.titulo, style = MaterialTheme.typography.titleLarge)
            Text(solicitud.descripcion, style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Dato("Categoría", solicitud.categoria)
            if (solicitud.servicio.isNotBlank()) Dato("Servicio", solicitud.servicio)
            Dato("Prioridad", solicitud.prioridad ?: "Sin definir")
            Dato("Técnico", nombreTecnico ?: "Sin asignar")
            Dato("Creada", formatearFecha(solicitud.fechaCreacion))
            Dato("Actualizada", formatearFecha(solicitud.fechaActualizacion))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TarjetaGestion(
    state: DetalleUiState,
    onPrioridad: (Prioridad) -> Unit,
    onAsignar: (com.example.solitrack.data.local.UsuarioEntity) -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Gestión del administrador", style = MaterialTheme.typography.titleSmall)

            if (state.puedePriorizar) {
                Text("Prioridad", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Prioridad.entries.forEach { prioridad ->
                        FilterChip(
                            selected = state.solicitud?.prioridad == prioridad.etiqueta,
                            onClick = { onPrioridad(prioridad) },
                            label = { Text(prioridad.etiqueta) },
                            // La tarjeta ya usa secondaryContainer: el chip elegido se pinta con primary para que se note
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            if (state.puedeAsignar) {
                var abierto by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
                    OutlinedTextField(
                        value = state.nombreTecnico ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (state.solicitud?.tecnicoId == null) "Asignar técnico" else "Reasignar técnico") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(abierto) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                        state.tecnicos.forEach { tecnico ->
                            DropdownMenuItem(
                                text = { Text(tecnico.nombre) },
                                onClick = {
                                    onAsignar(tecnico)
                                    abierto = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowAcciones(acciones: List<AccionEstado>, onClick: (AccionEstado) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        acciones.forEach { accion ->
            val esCancelar = accion.destino == EstadoSolicitud.CANCELADA
            Button(
                onClick = { onClick(accion) },
                colors = if (esCancelar) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ) else ButtonDefaults.buttonColors()
            ) { Text(accion.etiqueta) }
        }
    }
}

@Composable
private fun DialogoComentario(
    accion: AccionEstado,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(accion.etiqueta) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La solicitud pasará a «${accion.destino.etiqueta}».")
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = {
                        Text(if (accion.destino == EstadoSolicitud.CANCELADA) "Motivo" else "Comentario")
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(texto) }, enabled = texto.isNotBlank()) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun NuevoComentario(permiteInterno: Boolean, onEnviar: (String, Boolean) -> Unit) {
    var texto by rememberSaveable { mutableStateOf("") }
    var interno by rememberSaveable { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Escribe un comentario") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    onEnviar(texto, interno)
                    texto = ""
                },
                enabled = texto.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar comentario")
            }
        }
        // Solo admin y técnico pueden dejar notas que el cliente no ve
        if (permiteInterno) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = interno, onCheckedChange = { interno = it })
                Text("Interno (el cliente no lo ve)", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ComentarioItem(comentario: ComentarioEntity) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (comentario.interno) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${comentario.autorNombre} · ${comentario.autorRol}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                if (comentario.interno) {
                    Icon(Icons.Filled.Lock, contentDescription = "Interno", modifier = Modifier.size(14.dp))
                    Text(" Interno", style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(comentario.texto, style = MaterialTheme.typography.bodyMedium)
            Text(formatearFecha(comentario.fecha), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun HistorialItem(historial: HistorialEntity) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Filled.History,
            contentDescription = null,
            modifier = Modifier.size(18.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(historial.accion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            val cambio = if (historial.estadoAnterior == null || historial.estadoAnterior == historial.estadoNuevo)
                historial.estadoNuevo else "${historial.estadoAnterior} → ${historial.estadoNuevo}"
            Text(cambio, style = MaterialTheme.typography.bodySmall)
            Text(
                "${historial.usuarioNombre} (${historial.usuarioRol}) · ${formatearFecha(historial.fecha)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Seccion(titulo: String) {
    Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun TextoSecundario(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row {
        Text("$etiqueta: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Text(valor, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))
