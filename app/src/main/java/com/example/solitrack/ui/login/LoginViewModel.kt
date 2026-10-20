package com.example.solitrack.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solitrack.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(valor: String) {
        _uiState.update { it.copy(usuario = valor, usuarioError = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { it.copy(clave = valor, claveError = null, errorGeneral = null) }
    }

    fun onAlternarVisibilidadClave() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun iniciarSesion() {
        val actual = _uiState.value
        if (actual.cargando) return

        // 1) Validar campos
        val usuarioError = if (actual.usuario.isBlank()) "Ingresa tu usuario" else null
        val claveError = when {
            actual.clave.isBlank() -> "Ingresa tu contraseña"
            actual.clave.length < 4 -> "La contraseña debe tener al menos 4 caracteres"
            else -> null
        }

        if (usuarioError != null || claveError != null) {
            _uiState.update { it.copy(usuarioError = usuarioError, claveError = claveError) }
            return
        }

        // 2) Verificar credenciales
        _uiState.update { it.copy(cargando = true, errorGeneral = null) }

        viewModelScope.launch {
            delay(800) // Simula la espera de un servidor (se ve el indicador de carga)
            val valido = repository.login(actual.usuario.trim(), actual.clave)

            _uiState.update {
                if (valido) {
                    it.copy(cargando = false, loginExitoso = true)
                } else {
                    it.copy(cargando = false, errorGeneral = "Usuario o contraseña incorrectos")
                }
            }
        }
    }

    // La pantalla avisa que ya navegó, para no volver a navegar al recomponer
    fun onNavegacionRealizada() {
        _uiState.update { it.copy(loginExitoso = false, clave = "") }
    }
}
