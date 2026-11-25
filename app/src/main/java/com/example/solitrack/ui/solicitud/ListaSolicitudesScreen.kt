package com.example.solitrack.ui.solicitud


import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.model.EstadoSolicitud
import com.example.solitrack.data.model.Prioridad
import com.example.solitrack.data.model.Rol
import com.example.solitrack.ui.components.EstadoChip
import com.example.solitrack.ui.components.NotificacionUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    onNuevaSolicitud: () -> Unit,
    onAbrirSolicitud: (Long) -> Unit,
    onVolver: () -> Unit,
    viewModel: SolicitudViewModel = viewModel(factory = SolicitudViewModel.Factory)
) {
    // Cada vez que Room emite una lista nueva, esta pantalla se redibuja
    val todas by viewModel.solicitudes.collectAsState()
    val solicitudes by viewModel.solicitudesFiltradas.collectAsState()
    val filtros by viewModel.filtros.collectAsState()
    val usuario by viewModel.usuario.collectAsState()
    val rol = usuario?.rol

    // Qué puede hacer cada perfil en esta pantalla
    val puedeCrear = rol == Rol.CLIENTE
    val puedeEliminar = rol == Rol.CLIENTE || rol == Rol.ADMINISTRADOR

    // Solicitud que el usuario quiere eliminar (null = diálogo cerrado)
    var porEliminar by remember { mutableStateOf<SolicitudEntity?>(null) }

    porEliminar?.let { solicitud ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Eliminar solicitud") },
            text = { Text("¿Eliminar la solicitud ${solicitud.codigo}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(solicitud.id)
                    porEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // Permiso de notificaciones (Android 13+). Se pide al entrar a la lista;
    // si el usuario lo rechaza, la app funciona igual pero sin avisos.
    val context = LocalContext.current
    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (!NotificacionUtils.tienePermiso(context)) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (rol) {
                            Rol.ADMINISTRADOR -> "Todas las solicitudes"
                            Rol.TECNICO -> "Solicitudes asignadas"
                            else -> "Mis solicitudes"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            // Solo el cliente registra solicitudes nuevas
            if (puedeCrear) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaSolicitud,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva") }
                )
            }
        }
    ) { innerPadding ->

        val lista = solicitudes

        when {
            // Cargando desde Room
            lista == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // No hay solicitudes guardadas (sin contar filtros)
            todas.isNullOrEmpty() -> {
                ListaVacia(
                    mensaje = when (rol) {
                        Rol.CLIENTE -> "Presiona «Nueva» para registrar tu primera solicitud."
                        Rol.TECNICO -> "Todavía no tienes solicitudes asignadas."
                        else -> "Los clientes aún no registran solicitudes."
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Lista de solicitudes
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    // bottom = 88.dp para que el botón flotante no tape la última tarjeta
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        BarraFiltros(
                            filtros = filtros,
                            mostrarPrioridad = rol == Rol.ADMINISTRADOR,
                            onBusqueda = viewModel::onBusquedaChange,
                            onEstado = viewModel::onFiltroEstado,
                            onPrioridad = viewModel::onFiltroPrioridad
                        )
                    }

                    item {
                        Text(
                            text = if (filtros.activos) "${lista.size} de ${todas?.size ?: 0} solicitud(es)"
                            else "${lista.size} solicitud(es) registrada(s)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(lista, key = { it.id }) { solicitud ->
                        SolicitudCard(
                            solicitud = solicitud,
                            onClick = { onAbrirSolicitud(solicitud.id) },
                            // null = sin botón de eliminar (técnico)
                            onEliminar = if (puedeEliminar) ({ porEliminar = solicitud }) else null,
                            // Animación: las tarjetas se deslizan al aparecer o reordenarse
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SolicitudCard(
    solicitud: SolicitudEntity,
    onClick: () -> Unit,
    onEliminar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.codigo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EstadoChip(estado = solicitud.estado)
                    if (onEliminar != null) {
                        IconButton(onClick = onEliminar) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar ${solicitud.codigo}",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Category,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = solicitud.categoria,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                solicitud.prioridad?.let {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Prioridad $it",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (it == "Alta") MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }

                if (solicitud.evidenciaUri != null) {
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = "Tiene evidencia",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text(
                text = "Actualizada: ${formatearFecha(solicitud.fechaActualizacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BarraFiltros(
    filtros: FiltrosLista,
    mostrarPrioridad: Boolean,
    onBusqueda: (String) -> Unit,
    onEstado: (String) -> Unit,
    onPrioridad: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = filtros.busqueda,
            onValueChange = onBusqueda,
            label = { Text("Buscar por código o título") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EstadoSolicitud.entries) { estado ->
                FilterChip(
                    selected = filtros.estado == estado.etiqueta,
                    onClick = { onEstado(estado.etiqueta) },
                    label = { Text(estado.etiqueta) }
                )
            }
        }
        if (mostrarPrioridad) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Prioridad.entries) { prioridad ->
                    FilterChip(
                        selected = filtros.prioridad == prioridad.etiqueta,
                        onClick = { onPrioridad(prioridad.etiqueta) },
                        label = { Text("Prioridad ${prioridad.etiqueta}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ListaVacia(mensaje: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Inbox,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Aún no hay solicitudes",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))
