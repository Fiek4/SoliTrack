package com.example.solitrack.ui.menu

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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.Rol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    usuario: UsuarioEntity,
    onIrASolicitudes: () -> Unit,
    onIrACatalogo: () -> Unit,
    onIrAResumen: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SoliTrack") },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Hola, ${usuario.nombre.substringBefore(" ")} 👋",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(4.dp))
                // Etiqueta con el perfil que inició sesión
                AssistChip(
                    onClick = {},
                    label = { Text(usuario.rol.etiqueta) },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) }
                )
            }

            // Cada perfil ve opciones distintas (caso 009D)
            when (usuario.rol) {
                Rol.CLIENTE -> {
                    OpcionMenu(
                        icono = Icons.Filled.Inbox,
                        titulo = "Mis solicitudes",
                        descripcion = "Registra, sigue el estado e historial y comparte por WhatsApp",
                        habilitada = true,
                        onClick = onIrASolicitudes
                    )
                }

                Rol.ADMINISTRADOR -> {
                    OpcionMenu(
                        icono = Icons.Filled.Inbox,
                        titulo = "Todas las solicitudes",
                        descripcion = "Revisa, prioriza y asigna las solicitudes",
                        habilitada = true,
                        onClick = onIrASolicitudes
                    )
                    OpcionMenu(
                        icono = Icons.Filled.Storefront,
                        titulo = "Catálogo de servicios",
                        descripcion = "Servicios desde la API REST (GET, POST, PUT, DELETE)",
                        habilitada = true,
                        onClick = onIrACatalogo
                    )
                    OpcionMenu(
                        icono = Icons.Filled.BarChart,
                        titulo = "Resumen",
                        descripcion = "Totales por estado y por prioridad",
                        habilitada = true,
                        onClick = onIrAResumen
                    )
                }

                Rol.TECNICO -> {
                    OpcionMenu(
                        icono = Icons.Filled.Build,
                        titulo = "Solicitudes asignadas",
                        descripcion = "Atiende las solicitudes que te asignaron",
                        habilitada = true,
                        onClick = onIrASolicitudes
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpcionMenu(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        enabled = habilitada,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (habilitada) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icono,
                        contentDescription = null,
                        tint = if (habilitada) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (habilitada) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }
    }
}
