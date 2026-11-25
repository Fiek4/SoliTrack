package com.example.solitrack.ui.login

import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.Rol
import com.example.solitrack.data.repository.AuthRepository
import com.example.solitrack.data.repository.SesionRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : StringSpec({

    // El login tiene un delay(800) que simula al servidor.
    // StandardTestDispatcher + advanceUntilIdle() adelanta ese tiempo sin esperar de verdad.
    val dispatcher = StandardTestDispatcher()
    beforeTest { Dispatchers.setMain(dispatcher) }
    afterTest { Dispatchers.resetMain() }

    val tecnico = UsuarioEntity(id = 3, usuario = "tecnico", clave = "1234", nombre = "Tomás Díaz", rol = Rol.TECNICO)

    "con credenciales correctas inicia la sesión con el rol del usuario" {
        val auth = mockk<AuthRepository>()
        coEvery { auth.login("tecnico", "1234") } returns tecnico
        val sesion = SesionRepository()
        val vm = LoginViewModel(auth, sesion)

        vm.onUsuarioChange("tecnico")
        vm.onClaveChange("1234")
        vm.iniciarSesion()
        dispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.loginExitoso shouldBe true
        sesion.usuario.value?.rol shouldBe Rol.TECNICO
    }

    "con credenciales incorrectas muestra error y no inicia sesión" {
        val auth = mockk<AuthRepository>()
        coEvery { auth.login(any(), any()) } returns null
        val sesion = SesionRepository()
        val vm = LoginViewModel(auth, sesion)

        vm.onUsuarioChange("cliente")
        vm.onClaveChange("9999")
        vm.iniciarSesion()
        dispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.errorGeneral.shouldNotBeNull()
        vm.uiState.value.loginExitoso shouldBe false
        sesion.usuario.value.shouldBeNull()
    }

    "con campos vacíos valida sin consultar la base de datos" {
        val auth = mockk<AuthRepository>()
        val vm = LoginViewModel(auth, SesionRepository())

        vm.iniciarSesion()

        vm.uiState.value.usuarioError.shouldNotBeNull()
        vm.uiState.value.claveError.shouldNotBeNull()
        coVerify(exactly = 0) { auth.login(any(), any()) }
    }
})
