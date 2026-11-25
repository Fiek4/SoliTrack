package com.example.solitrack.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.solitrack.SoliTrackApp
import com.example.solitrack.data.local.ComentarioEntity
import com.example.solitrack.data.local.HistorialEntity
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.AccionEstado
import com.example.solitrack.data.model.FlujoEstados
import com.example.solitrack.data.model.Prioridad
import com.example.solitrack.data.repository.AuthRepository
import com.example.solitrack.data.repository.SesionRepository
import com.example.solitrack.data.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Todo lo que muestra la pantalla de detalle, ya filtrado según el rol. */
data class DetalleUiState(
    val cargando: Boolean = true,
    val solicitud: SolicitudEntity? = null,
    val usuario: UsuarioEntity? = null,
    val historial: List<HistorialEntity> = emptyList(),
    val comentarios: List<ComentarioEntity> = emptyList(),
    val tecnicos: List<UsuarioEntity> = emptyList(),

    // Permisos calculados con FlujoEstados
    val acciones: List<AccionEstado> = emptyList(),
    val puedeAsignar: Boolean = false,
    val puedePriorizar: Boolean = false,
    val puedeEditar: Boolean = false,
    val puedeComentarInterno: Boolean = false
) {
    val nombreTecnico: String?
        get() = tecnicos.find { it.id == solicitud?.tecnicoId }?.nombre
}

class DetalleViewModel(
    private val solicitudId: Long,
    private val repository: SolicitudRepository,
    private val authRepository: AuthRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val tecnicos = MutableStateFlow<List<UsuarioEntity>>(emptyList())

    // Mensaje corto para el Snackbar (null = nada que mostrar)
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // combine junta 5 Flows: cada vez que cambia uno (ej: Room guarda un nuevo estado)
    // se recalcula el estado completo de la pantalla
    val uiState: StateFlow<DetalleUiState> = combine(
        repository.observarPorId(solicitudId),
        repository.observarHistorial(solicitudId),
        repository.observarComentarios(solicitudId),
        sesion.usuario,
        tecnicos
    ) { solicitud, historial, comentarios, usuario, listaTecnicos ->
        if (solicitud == null || usuario == null) {
            DetalleUiState(cargando = false, solicitud = solicitud, usuario = usuario)
        } else {
            DetalleUiState(
                cargando = false,
                solicitud = solicitud,
                usuario = usuario,
                historial = historial,
                // El cliente no ve los comentarios internos
                comentarios = if (FlujoEstados.puedeVerInternos(usuario)) comentarios
                else comentarios.filterNot { it.interno },
                tecnicos = listaTecnicos,
                acciones = FlujoEstados.accionesDisponibles(usuario, solicitud),
                puedeAsignar = FlujoEstados.puedeAsignar(usuario, solicitud),
                puedePriorizar = FlujoEstados.puedePriorizar(usuario, solicitud),
                puedeEditar = FlujoEstados.puedeEditarDatos(usuario, solicitud),
                puedeComentarInterno = FlujoEstados.puedeVerInternos(usuario)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState())

    init {
        viewModelScope.launch {
            tecnicos.value = try { authRepository.obtenerTecnicos() } catch (e: Exception) { emptyList() }
        }
    }

    // ---------- ACCIONES ----------

    fun ejecutarAccion(accion: AccionEstado, comentario: String?) {
        val usuario = sesion.usuario.value ?: return
        if (accion.requiereComentario && comentario.isNullOrBlank()) {
            _mensaje.value = "Escribe un comentario para continuar"
            return
        }
        lanzar("Estado actualizado: ${accion.destino.etiqueta}") {
            repository.cambiarEstado(solicitudId, accion.destino, accion.etiqueta, usuario, comentario)
        }
    }

    fun asignarTecnico(tecnico: UsuarioEntity) {
        val admin = sesion.usuario.value ?: return
        if (uiState.value.solicitud?.tecnicoId == tecnico.id) return
        lanzar("Asignada a ${tecnico.nombre}") {
            repository.asignarTecnico(solicitudId, tecnico, admin)
        }
    }

    fun definirPrioridad(prioridad: Prioridad) {
        val admin = sesion.usuario.value ?: return
        lanzar("Prioridad ${prioridad.etiqueta}") {
            repository.definirPrioridad(solicitudId, prioridad, admin)
        }
    }

    fun comentar(texto: String, interno: Boolean) {
        val usuario = sesion.usuario.value ?: return
        if (texto.isBlank()) return
        // Por seguridad, un cliente nunca crea comentarios internos
        val esInterno = interno && FlujoEstados.puedeVerInternos(usuario)
        lanzar("Comentario agregado") {
            repository.agregarComentario(solicitudId, usuario, texto, esInterno)
        }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    private fun lanzar(exito: String, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            _mensaje.value = try {
                bloque()
                exito
            } catch (e: Exception) {
                "No se pudo guardar el cambio"
            }
        }
    }

    companion object {
        // El id de la solicitud llega por la ruta de navegación
        fun factory(solicitudId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SoliTrackApp
                DetalleViewModel(solicitudId, app.solicitudRepository, app.authRepository, app.sesion)
            }
        }
    }
}
