package com.example.solitrack.ui.catalogo

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solitrack.data.model.Categoria
import com.example.solitrack.data.model.Servicio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onVolver: () -> Unit,
    viewModel: CatalogoViewModel = viewModel(factory = CatalogoViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Servicio que el usuario quiere eliminar (null = diálogo cerrado)
    var porEliminar by remember { mutableStateOf<Servicio?>(null) }

    // Cada respuesta del servidor se muestra una vez en un Snackbar
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    state.enEdicion?.let { servicio ->
        DialogoServicio(
            servicio = servicio,
            nombreError = state.nombreError,
            categoriaError = state.categoriaError,
            enviando = state.enviando,
            onNombreChange = viewModel::onNombreChange,
            onCategoriaChange = viewModel::onCategoriaChange,
            onDescripcionChange = viewModel::onDescripcionChange,
            onGuardar = viewModel::guardar,
            onCancelar = viewModel::cerrarDialogo
        )
    }

    porEliminar?.let { servicio ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Eliminar servicio") },
            text = { Text("¿Eliminar \"${servicio.nombre}\" del catálogo?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(servicio)
                    porEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de servicios") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.cargando && state.error == null) {
                FloatingActionButton(onClick = viewModel::abrirNuevo) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo servicio")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(state.error.orEmpty(), textAlign = TextAlign.Center)
                    TextButton(onClick = viewModel::cargar) { Text("Reintentar") }
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "API de prueba: los cambios no se guardan en el servidor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(state.servicios, key = { it.id }) { servicio ->
                        ServicioCard(
                            servicio = servicio,
                            onEditar = { viewModel.abrirEdicion(servicio) },
                            onEliminar = { porEliminar = servicio }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicioCard(
    servicio: Servicio,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(servicio.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    servicio.categoria,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (servicio.descripcion.isNotBlank()) {
                    Text(
                        servicio.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar ${servicio.nombre}")
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar ${servicio.nombre}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoServicio(
    servicio: Servicio,
    nombreError: String?,
    categoriaError: String?,
    enviando: Boolean,
    onNombreChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val esNuevo = servicio.id == 0
    var menuAbierto by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!enviando) onCancelar() },
        title = { Text(if (esNuevo) "Nuevo servicio (POST)" else "Editar servicio (PUT)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = servicio.nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    isError = nombreError != null,
                    supportingText = nombreError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = menuAbierto,
                    onExpandedChange = { menuAbierto = it }
                ) {
                    OutlinedTextField(
                        value = servicio.categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        isError = categoriaError != null,
                        supportingText = categoriaError?.let { { Text(it) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuAbierto) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        Categoria.entries.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.etiqueta) },
                                onClick = {
                                    onCategoriaChange(categoria.etiqueta)
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = servicio.descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onGuardar, enabled = !enviando) {
                if (enviando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(if (esNuevo) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !enviando) { Text("Cancelar") }
        }
    )
}
