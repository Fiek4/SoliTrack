package com.example.solitrack.ui.solicitud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.solitrack.SoliTrackApp
import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.Rol
import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.repository.ServicioRepository
import com.example.solitrack.data.repository.SesionRepository
import com.example.solitrack.data.repository.SolicitudRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudViewModel(
    private val repository: SolicitudRepository,
    private val servicioRepository: ServicioRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    // Usuario que inició sesión: la pantalla lo usa para mostrar/ocultar acciones según el rol
    val usuario: StateFlow<UsuarioEntity?> = sesion.usuario

    // ---------- LISTA ----------
    // Cada rol ve solicitudes distintas. flatMapLatest cambia de consulta
    // si cambia el usuario de la sesión.
    // null = todavía cargando desde Room / lista = datos listos
    val solicitudes: StateFlow<List<SolicitudEntity>?> = sesion.usuario
        .flatMapLatest { usuario ->
            when (usuario?.rol) {
                Rol.CLIENTE -> repository.obtenerPorCliente(usuario.id)
                Rol.TECNICO -> repository.obtenerPorTecnico(usuario.id)
                Rol.ADMINISTRADOR -> repository.obtenerTodas()
                null -> flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    // ---------- FORMULARIO ----------
    // Privado y mutable: solo el ViewModel lo cambia
    private val _uiState = MutableStateFlow(SolicitudUiState())
    // Público y de solo lectura: la pantalla solo lo observa
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    // Evita recargar el formulario si la pantalla se recompone (ej: al girar)
    private var formularioIniciado = false

    /**
     * id == null -> formulario vacío (nueva solicitud)
     * id != null -> carga la solicitud desde Room para editarla
     */
    fun iniciarFormulario(id: Long?) {
        if (formularioIniciado) return
        formularioIniciado = true

        // El catálogo de servicios se pide a la API en paralelo a Room
        cargarServicios()

        if (id == null) return

        viewModelScope.launch {
            repository.obtenerPorId(id)?.let { solicitud ->
                // update + copy: así no se pierde el catálogo si llegó antes
                _uiState.update {
                    it.copy(
                        id = solicitud.id,
                        codigo = solicitud.codigo,
                        titulo = solicitud.titulo,
                        descripcion = solicitud.descripcion,
                        categoria = solicitud.categoria,
                        servicio = solicitud.servicio,
                        evidenciaUri = solicitud.evidenciaUri,
                        estado = solicitud.estado
                    )
                }
            }
        }
    }

    // ---------- API REST: CATÁLOGO DE SERVICIOS ----------
    // cargando -> lista o error. Sin internet la app no se cae:
    // se muestra el error y el usuario puede escribir el servicio a mano.
    fun cargarServicios() {
        _uiState.update { it.copy(cargandoServicios = true, errorServicios = null) }

        viewModelScope.launch {
            try {
                val servicios = servicioRepository.obtenerServicios()
                _uiState.update { it.copy(servicios = servicios, cargandoServicios = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargandoServicios = false,
                        errorServicios = "No se pudo cargar el catálogo de servicios"
                    )
                }
            }
        }
    }

    // ---------- EVENTOS DE LA PANTALLA ----------
    // Al escribir se borra el error de ese campo

    fun onTituloChange(valor: String) {
        _uiState.update { it.copy(titulo = valor, tituloError = null) }
    }

    fun onDescripcionChange(valor: String) {
        _uiState.update { it.copy(descripcion = valor, descripcionError = null) }
    }

    fun onCategoriaChange(valor: String) {
        // Al cambiar la categoría también se limpia el error de servicio,
        // porque la obligatoriedad del servicio depende de la categoría
        _uiState.update { it.copy(categoria = valor, categoriaError = null, servicioError = null) }
    }

    fun onServicioChange(valor: String) {
        _uiState.update { it.copy(servicio = valor, servicioError = null) }
    }

    // Al elegir un servicio del catálogo; si aún no hay categoría, se completa sola
    fun onServicioSeleccionado(servicio: Servicio) {
        _uiState.update {
            it.copy(
                servicio = servicio.nombre,
                servicioError = null,
                categoria = it.categoria.ifBlank { servicio.categoria },
                categoriaError = if (it.categoria.isBlank()) null else it.categoriaError
            )
        }
    }

    fun onEvidenciaCapturada(uri: String) {
        _uiState.update { it.copy(evidenciaUri = uri) }
    }

    fun onQuitarEvidencia() {
        _uiState.update { it.copy(evidenciaUri = null) }
    }

    // ---------- GUARDAR ----------

    fun guardar() {
        val actual = _uiState.value
        if (actual.guardando) return

        // 1) Validar TODOS los campos
        val validado = actual.copy(
            tituloError = SolicitudValidator.validarTitulo(actual.titulo),
            descripcionError = SolicitudValidator.validarDescripcion(actual.descripcion),
            categoriaError = SolicitudValidator.validarCategoria(actual.categoria),
            servicioError = SolicitudValidator.validarServicio(actual.categoria, actual.servicio),
            errorGeneral = null
        )

        // 2) Si hay errores, se muestran en los campos y NO se guarda
        if (validado.tieneErrores) {
            _uiState.value = validado
            return
        }

        // 3) Todo OK -> guardar en Room en segundo plano (corrutina)
        _uiState.value = validado.copy(guardando = true)

        viewModelScope.launch {
            try {
                if (actual.id == null) {
                    val codigo = repository.crear(
                        titulo = actual.titulo.trim(),
                        descripcion = actual.descripcion.trim(),
                        categoria = actual.categoria,
                        servicio = actual.servicio.trim(),
                        evidenciaUri = actual.evidenciaUri,
                        // La solicitud queda a nombre del cliente que inició sesión
                        clienteId = sesion.usuario.value?.id ?: 0
                    )
                    _uiState.update { it.copy(codigo = codigo) }
                } else {
                    repository.actualizar(
                        id = actual.id,
                        titulo = actual.titulo.trim(),
                        descripcion = actual.descripcion.trim(),
                        categoria = actual.categoria,
                        servicio = actual.servicio.trim(),
                        evidenciaUri = actual.evidenciaUri
                    )
                }
                // La pantalla observa este flag para mostrar la confirmación y volver
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "No se pudo guardar la solicitud. Intenta nuevamente."
                    )
                }
            }
        }
    }

    // ---------- ELIMINAR ----------
    // No hace falta actualizar la lista a mano: Room emite la lista nueva
    fun eliminar(id: Long) {
        viewModelScope.launch {
            repository.eliminar(id)
        }
    }

    // ---------- FACTORY ----------
    // Le dice a Compose cómo crear este ViewModel entregándole el Repository
    // que vive en SoliTrackApp. Uso: viewModel(factory = SolicitudViewModel.Factory)
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SoliTrackApp
                SolicitudViewModel(app.solicitudRepository, app.servicioRepository, app.sesion)
            }
        }
    }
}
