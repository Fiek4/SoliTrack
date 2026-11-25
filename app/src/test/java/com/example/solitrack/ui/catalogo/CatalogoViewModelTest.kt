package com.example.solitrack.ui.catalogo

import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.repository.ServicioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModelTest : StringSpec({

    // viewModelScope usa Dispatchers.Main, que no existe en la JVM de los tests.
    // Se reemplaza por un dispatcher de prueba que ejecuta las corrutinas al instante.
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val hosting = Servicio(1, "Hosting básico", "Hosting", "Alojamiento web")
    val seo = Servicio(5, "Posicionamiento SEO", "Marketing digital", "Visibilidad en buscadores")

    fun repositorioCon(servicios: List<Servicio>): ServicioRepository {
        val repo = mockk<ServicioRepository>()
        coEvery { repo.obtenerServicios() } returns servicios
        return repo
    }

    "al iniciar carga el catálogo desde el repositorio" {
        val vm = CatalogoViewModel(repositorioCon(listOf(hosting, seo)))

        vm.uiState.value.servicios shouldBe listOf(hosting, seo)
        vm.uiState.value.cargando shouldBe false
        vm.uiState.value.error.shouldBeNull()
    }

    "si la API falla muestra un error y la app no se cae" {
        val repo = mockk<ServicioRepository>()
        coEvery { repo.obtenerServicios() } throws java.io.IOException("Sin internet")

        val vm = CatalogoViewModel(repo)

        vm.uiState.value.error.shouldNotBeNull()
        vm.uiState.value.servicios shouldHaveSize 0
    }

    "guardar con campos vacíos muestra errores y no llama a la API" {
        val repo = repositorioCon(listOf(hosting))
        val vm = CatalogoViewModel(repo)

        vm.abrirNuevo()
        vm.guardar()

        vm.uiState.value.nombreError.shouldNotBeNull()
        vm.uiState.value.categoriaError.shouldNotBeNull()
        coVerify(exactly = 0) { repo.crear(any()) }
    }

    "POST: el servicio nuevo se agrega con un id que no se repite" {
        val repo = repositorioCon(listOf(hosting, seo))
        // La API de prueba siempre responde id 101
        coEvery { repo.crear(any()) } answers { firstArg<Servicio>().copy(id = 101) }
        val vm = CatalogoViewModel(repo)

        vm.abrirNuevo()
        vm.onNombreChange("Backup en la nube")
        vm.onCategoriaChange("Hosting")
        vm.guardar()

        val estado = vm.uiState.value
        estado.servicios shouldHaveSize 3
        estado.servicios.last().id shouldBe 6          // max(1, 5) + 1
        estado.servicios.last().nombre shouldBe "Backup en la nube"
        estado.enEdicion.shouldBeNull()                // el diálogo se cierra
        estado.mensaje.shouldNotBeNull() shouldStartWith "POST"
    }

    "PUT: el servicio editado reemplaza al original" {
        val repo = repositorioCon(listOf(hosting, seo))
        coEvery { repo.actualizar(any()) } answers { firstArg() }
        val vm = CatalogoViewModel(repo)

        vm.abrirEdicion(hosting)
        vm.onNombreChange("Hosting básico Plus")
        vm.guardar()

        val nombres = vm.uiState.value.servicios.map { it.nombre }
        nombres shouldContain "Hosting básico Plus"
        nombres shouldNotContain "Hosting básico"
    }

    "DELETE: el servicio desaparece de la lista" {
        val repo = repositorioCon(listOf(hosting, seo))
        coEvery { repo.eliminar(1) } returns Unit
        val vm = CatalogoViewModel(repo)

        vm.eliminar(hosting)

        vm.uiState.value.servicios shouldBe listOf(seo)
        coVerify { repo.eliminar(1) }
    }
})
