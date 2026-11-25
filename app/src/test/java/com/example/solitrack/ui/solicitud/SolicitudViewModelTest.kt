package com.example.solitrack.ui.solicitud

import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.repository.ServicioRepository
import com.example.solitrack.data.repository.SolicitudRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val hosting = Servicio(1, "Hosting básico", "Hosting", "Alojamiento web")
    val seo = Servicio(5, "Posicionamiento SEO", "Marketing digital", "Visibilidad en buscadores")

    // Mocks de los dos repositorios: no se usa Room ni internet
    fun crearViewModel(
        solicitudRepo: SolicitudRepository = mockk<SolicitudRepository>().also {
            every { it.obtenerTodas() } returns flowOf(emptyList())
        },
        servicioRepo: ServicioRepository = mockk<ServicioRepository>().also {
            coEvery { it.obtenerServicios() } returns listOf(hosting, seo)
        }
    ) = SolicitudViewModel(solicitudRepo, servicioRepo)

    "al abrir el formulario se carga el catálogo de servicios de la API" {
        val vm = crearViewModel()

        vm.iniciarFormulario(null)

        vm.uiState.value.servicios shouldBe listOf(hosting, seo)
        vm.uiState.value.cargandoServicios shouldBe false
    }

    "sin internet se muestra error de catálogo y el formulario sigue funcionando" {
        val servicioRepo = mockk<ServicioRepository>()
        coEvery { servicioRepo.obtenerServicios() } throws java.io.IOException("Sin internet")
        val vm = crearViewModel(servicioRepo = servicioRepo)

        vm.iniciarFormulario(null)
        vm.onTituloChange("Sitio caído")

        vm.uiState.value.errorServicios.shouldNotBeNull()
        vm.uiState.value.titulo shouldBe "Sitio caído"
    }

    "las sugerencias se filtran por la categoría elegida" {
        val vm = crearViewModel()
        vm.iniciarFormulario(null)

        vm.onCategoriaChange("Hosting")

        vm.uiState.value.serviciosSugeridos shouldBe listOf(hosting)
    }

    "elegir un servicio del catálogo completa la categoría si estaba vacía" {
        val vm = crearViewModel()

        vm.onServicioSeleccionado(seo)

        vm.uiState.value.servicio shouldBe "Posicionamiento SEO"
        vm.uiState.value.categoria shouldBe "Marketing digital"
    }

    "guardar un formulario vacío muestra errores y no guarda en Room" {
        val solicitudRepo = mockk<SolicitudRepository>()
        every { solicitudRepo.obtenerTodas() } returns flowOf(emptyList())
        val vm = crearViewModel(solicitudRepo = solicitudRepo)

        vm.guardar()

        vm.uiState.value.tituloError.shouldNotBeNull()
        vm.uiState.value.guardadoExitosamente shouldBe false
        coVerify(exactly = 0) { solicitudRepo.crear(any(), any(), any(), any(), any()) }
    }

    "guardar un formulario válido crea la solicitud y entrega su código" {
        val solicitudRepo = mockk<SolicitudRepository>()
        every { solicitudRepo.obtenerTodas() } returns flowOf(emptyList())
        coEvery { solicitudRepo.crear(any(), any(), any(), any(), any()) } returns "SOL-0007"
        val vm = crearViewModel(solicitudRepo = solicitudRepo)

        vm.onTituloChange("Sitio web caído")
        vm.onDescripcionChange("El sitio de la empresa no carga desde la mañana")
        vm.onCategoriaChange("Hosting")
        vm.onServicioSeleccionado(hosting)
        vm.guardar()

        vm.uiState.value.errorGeneral.shouldBeNull()
        vm.uiState.value.guardadoExitosamente shouldBe true
        vm.uiState.value.codigo shouldBe "SOL-0007"
    }

    "eliminar le pide al repositorio borrar la solicitud" {
        val solicitudRepo = mockk<SolicitudRepository>()
        every { solicitudRepo.obtenerTodas() } returns flowOf(emptyList())
        coEvery { solicitudRepo.eliminar(3) } returns Unit
        val vm = crearViewModel(solicitudRepo = solicitudRepo)

        vm.eliminar(3)

        coVerify(exactly = 1) { solicitudRepo.eliminar(3) }
    }
})
