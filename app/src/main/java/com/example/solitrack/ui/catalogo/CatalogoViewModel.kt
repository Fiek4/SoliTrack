package com.example.solitrack.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.solitrack.SoliTrackApp
import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.repository.ServicioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CRUD del catálogo contra la API REST: GET, POST, PUT y DELETE.
// La API de prueba no guarda los cambios, por eso la lista se actualiza
// aquí en memoria con lo que responde el servidor. Al volver a entrar
// se recarga el catálogo original.
class CatalogoViewModel(private val repository: ServicioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    // ---------- GET ----------
    fun cargar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val servicios = repository.obtenerServicios()
                _uiState.update { it.copy(servicios = servicios, cargando = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo cargar el catálogo de servicios")
                }
            }
        }
    }

    // ---------- DIÁLOGO ----------
    fun abrirNuevo() {
        _uiState.update {
            it.copy(
                enEdicion = Servicio(id = 0, nombre = "", categoria = "", descripcion = ""),
                nombreError = null,
                categoriaError = null
            )
        }
    }

    fun abrirEdicion(servicio: Servicio) {
        _uiState.update { it.copy(enEdicion = servicio, nombreError = null, categoriaError = null) }
    }

    fun cerrarDialogo() {
        _uiState.update { it.copy(enEdicion = null) }
    }

    fun onNombreChange(valor: String) {
        _uiState.update { it.copy(enEdicion = it.enEdicion?.copy(nombre = valor), nombreError = null) }
    }

    fun onCategoriaChange(valor: String) {
        _uiState.update { it.copy(enEdicion = it.enEdicion?.copy(categoria = valor), categoriaError = null) }
    }

    fun onDescripcionChange(valor: String) {
        _uiState.update { it.copy(enEdicion = it.enEdicion?.copy(descripcion = valor)) }
    }

    // ---------- POST / PUT ----------
    fun guardar() {
        val actual = _uiState.value
        val servicio = actual.enEdicion ?: return
        if (actual.enviando) return

        val nombreError = if (servicio.nombre.isBlank()) "Ingresa el nombre del servicio" else null
        val categoriaError = if (servicio.categoria.isBlank()) "Selecciona una categoría" else null
        if (nombreError != null || categoriaError != null) {
            _uiState.update { it.copy(nombreError = nombreError, categoriaError = categoriaError) }
            return
        }

        val limpio = servicio.copy(nombre = servicio.nombre.trim(), descripcion = servicio.descripcion.trim())
        _uiState.update { it.copy(enviando = true) }

        viewModelScope.launch {
            try {
                if (limpio.id == 0) {
                    // POST: la API de prueba siempre responde id 101, así que el id
                    // definitivo se calcula aquí para que no se repita en la lista
                    val respuesta = repository.crear(limpio)
                    val nuevo = respuesta.copy(id = (actual.servicios.maxOfOrNull { it.id } ?: 0) + 1)
                    _uiState.update {
                        it.copy(
                            servicios = it.servicios + nuevo,
                            enEdicion = null,
                            enviando = false,
                            mensaje = "POST: servidor creó \"${nuevo.nombre}\" (id ${respuesta.id})"
                        )
                    }
                } else {
                    // PUT: se reemplaza el servicio por lo que devolvió el servidor
                    val respuesta = repository.actualizar(limpio)
                    _uiState.update {
                        it.copy(
                            servicios = it.servicios.map { s -> if (s.id == respuesta.id) respuesta else s },
                            enEdicion = null,
                            enviando = false,
                            mensaje = "PUT: servidor actualizó \"${respuesta.nombre}\""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(enviando = false, mensaje = "No se pudo conectar con el servidor")
                }
            }
        }
    }

    // ---------- DELETE ----------
    fun eliminar(servicio: Servicio) {
        viewModelScope.launch {
            try {
                repository.eliminar(servicio.id)
                _uiState.update {
                    it.copy(
                        servicios = it.servicios.filter { s -> s.id != servicio.id },
                        mensaje = "DELETE: servidor eliminó \"${servicio.nombre}\""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo eliminar: sin conexión con el servidor") }
            }
        }
    }

    // La pantalla avisa que ya mostró el Snackbar
    fun mensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SoliTrackApp
                CatalogoViewModel(app.servicioRepository)
            }
        }
    }
}
