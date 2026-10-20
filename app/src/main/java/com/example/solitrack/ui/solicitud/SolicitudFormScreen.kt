package com.example.solitrack.ui.solicitud

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.solitrack.data.model.Categoria
import com.example.solitrack.ui.components.CamaraUtils
import com.example.solitrack.ui.components.EstadoChip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    solicitudId: Long?,
    onVolver: () -> Unit,
    viewModel: SolicitudViewModel = viewModel(factory = SolicitudViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Carga la solicitud si viene un id (modo edición)
    LaunchedEffect(solicitudId) {
        viewModel.iniciarFormulario(solicitudId)
    }

    // Al guardar: se muestra la confirmación animada y luego se vuelve a la lista
    LaunchedEffect(state.guardadoExitosamente) {
        if (state.guardadoExitosamente) {
            delay(1200)
            onVolver()
        }
    }

    // Errores que no son de un campo (ej: fallo al guardar)
    LaunchedEffect(state.errorGeneral) {
        state.errorGeneral?.let { snackbarHostState.showSnackbar(it) }
    }

    // ======================== CÁMARA ========================
    // Uri del archivo donde la cámara guardará la foto.
    // rememberSaveable: no se pierde si Android recrea la pantalla
    // mientras la cámara está abierta.
    var uriFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { fotoTomada ->
        val uri = uriFotoPendiente
        if (fotoTomada && uri != null) {
            viewModel.onEvidenciaCapturada(uri)
        }
        uriFotoPendiente = null
    }

    fun abrirCamara() {
        val uri = CamaraUtils.crearUriParaFoto(context)
        uriFotoPendiente = uri.toString()
        camaraLauncher.launch(uri)
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Se necesita el permiso de cámara para adjuntar la evidencia")
            }
        }
    }

    val onTomarFoto: () -> Unit = {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) abrirCamara() else permisoLauncher.launch(Manifest.permission.CAMERA)
    }
    // ========================================================

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.esEdicion) "Editar ${state.codigo}" else "Nueva solicitud") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ---------- Encabezado ----------
                if (state.esEdicion) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Estado actual:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        EstadoChip(estado = state.estado)
                    }
                } else {
                    MensajeInformativo(
                        "Al registrarla quedará en estado «Recibida» y obtendrás un código de seguimiento."
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ---------- Título ----------
                CampoFormulario(
                    valor = state.titulo,
                    onValorChange = viewModel::onTituloChange,
                    etiqueta = "Título *",
                    icono = Icons.Filled.Title,
                    error = state.tituloError,
                    ayuda = "Ej: El sitio web no carga"
                )

                // ---------- Descripción ----------
                CampoFormulario(
                    valor = state.descripcion,
                    onValorChange = viewModel::onDescripcionChange,
                    etiqueta = "Descripción *",
                    icono = Icons.Filled.Description,
                    error = state.descripcionError,
                    ayuda = "Describe la necesidad con el mayor detalle posible",
                    singleLine = false,
                    minLines = 4
                )

                // ---------- Categoría ----------
                SelectorCategoria(
                    seleccionada = state.categoria,
                    error = state.categoriaError,
                    onSeleccionar = viewModel::onCategoriaChange
                )

                // ---------- Servicio relacionado ----------
                val requiereServicio = Categoria.desdeEtiqueta(state.categoria)?.requiereServicio == true
                CampoFormulario(
                    valor = state.servicio,
                    onValorChange = viewModel::onServicioChange,
                    etiqueta = if (requiereServicio) "Servicio o sitio relacionado *" else "Servicio o sitio relacionado",
                    icono = Icons.Filled.Language,
                    error = state.servicioError,
                    ayuda = if (requiereServicio) "Obligatorio para esta categoría (ej: www.miempresa.cl)" else "Opcional"
                )

                Spacer(Modifier.height(8.dp))

                // ---------- Evidencia (cámara) ----------
                SeccionEvidencia(
                    evidenciaUri = state.evidenciaUri,
                    onTomarFoto = onTomarFoto,
                    onQuitar = viewModel::onQuitarEvidencia
                )

                Spacer(Modifier.height(16.dp))

                // ---------- Guardar ----------
                Button(
                    onClick = viewModel::guardar,
                    enabled = !state.guardando && !state.guardadoExitosamente,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (state.guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Guardando...")
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.esEdicion) "Guardar cambios" else "Registrar solicitud")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Capa de confirmación animada (encima del formulario)
            ConfirmacionGuardado(
                visible = state.guardadoExitosamente,
                codigo = state.codigo,
                esEdicion = state.esEdicion
            )
        }
    }
}

// ============================================================
// Campo de texto reutilizable con validación visual:
//   - borde rojo (isError)
//   - ícono de error animado a la derecha
//   - texto de ayuda que cambia (animado) al mensaje de error
// ============================================================
@Composable
private fun CampoFormulario(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    ayuda: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(
                visible = error != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        isError = error != null,
        supportingText = {
            AnimatedContent(targetState = error ?: ayuda, label = "textoDeApoyo") { texto ->
                Text(texto)
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        modifier = Modifier.fillMaxWidth()
    )
}

// ============================================================
// Lista desplegable de categorías (Categoria.entries)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCategoria(
    seleccionada: String,
    error: String?,
    onSeleccionar: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {
        OutlinedTextField(
            value = seleccionada,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría *") },
            leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                }
            },
            isError = error != null,
            supportingText = { Text(error ?: "Selecciona el tipo de requerimiento") },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            Categoria.entries.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria.etiqueta) },
                    onClick = {
                        onSeleccionar(categoria.etiqueta)
                        expandido = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ============================================================
// Sección de evidencia fotográfica (recurso nativo: cámara)
// ============================================================
@Composable
private fun SeccionEvidencia(
    evidenciaUri: String?,
    onTomarFoto: () -> Unit,
    onQuitar: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Evidencia fotográfica", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                "Adjunta una foto ficticia que ayude a entender la solicitud (opcional).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // La foto aparece / desaparece con animación
            AnimatedVisibility(
                visible = evidenciaUri != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AsyncImage(
                    model = evidenciaUri,
                    contentDescription = "Foto de evidencia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onTomarFoto) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (evidenciaUri == null) "Tomar foto" else "Cambiar foto")
                }

                if (evidenciaUri != null) {
                    OutlinedButton(onClick = onQuitar) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Quitar")
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeInformativo(texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(texto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ============================================================
// Confirmación animada al guardar: aparece con efecto "rebote"
// ============================================================
@Composable
private fun ConfirmacionGuardado(
    visible: Boolean,
    codigo: String,
    esEdicion: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(
            initialScale = 0.6f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (esEdicion) "Cambios guardados" else "¡Solicitud registrada!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    if (codigo.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Código de seguimiento: $codigo",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
