package com.example.solitrack.ui.solicitud

import com.example.solitrack.data.local.SolicitudEntity
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.Rol
import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.repository.ServicioRepository
import com.example.solitrack.data.repository.SesionRepository
import com.example.solitrack.data.repository.SolicitudRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

    // Un usuario de prueba por perfil
    val cliente = UsuarioEntity(id = 1, usuario = "cliente", clave = "1234", nombre = "Camila Rojas", rol = Rol.CLIENTE)
    val admin = UsuarioEntity(id = 2, usuario = "admin", clave = "1234", nombre = "Andrés Soto", rol = Rol.ADMINISTRADOR)
    val tecnico = UsuarioEntity(id = 3, usuario = "tecnico", clave = "1234", nombre = "Tomás Díaz", rol = Rol.TECNICO)

    fun solicitud(id: Long, clienteId: Long = 1, tecnicoId: Long? = null) = SolicitudEntity(
        id = id, codigo = "SOL-000$id", titulo = "Solicitud $id", descripcion = "Descripción de prueba",
        categoria = "Hosting", servicio = "Hosting básico", clienteId = clienteId, tecnicoId = tecnicoId,
        fechaCreacion = 0, fechaActualizacion = 0
    )

    // Repositorio de solicitudes falso: por defecto todas las consultas devuelven listas vacías
    fun repoSolicitudes() = mockk<SolicitudRepository>().also {
        every { it.obtenerTodas() } returns flowOf(emptyList())
        every { it.obtenerPorCliente(any()) } returns flowOf(emptyList())
        every { it.obtenerPorTecnico(any()) } returns flowOf(emptyList())
    }

    // Mocks de los repositorios + sesión iniciada con el usuario indicado
    fun crearViewModel(
        solicitudRepo: SolicitudRepository = repoSolicitudes(),
        servicioRepo: ServicioRepository = mockk<ServicioRepository>().also {
            coEvery { it.obtenerServicios() } returns listOf(hosting, seo)
        },
        usuario: UsuarioEntity? = cliente
    ) = SolicitudViewModel(
        solicitudRepo,
        servicioRepo,
        SesionRepository().apply { usuario?.let { iniciar(it) } }
    )

    // ---------- LISTA SEGÚN EL ROL ----------

    "el cliente solo ve sus propias solicitudes" {
        val repo = repoSolicitudes()
        every { repo.obtenerPorCliente(1) } returns flowOf(listOf(solicitud(1)))
        val vm = crearViewModel(solicitudRepo = repo, usuario = cliente)

        vm.solicitudes.first { it != null } shouldBe listOf(solicitud(1))
        verify(exactly = 0) { repo.obtenerTodas() }
    }

    "el administrador ve todas las solicitudes" {
        val repo = repoSolicitudes()
        every { repo.obtenerTodas() } returns flowOf(listOf(solicitud(1), solicitud(2, clienteId = 9)))
        val vm = crearViewModel(solicitudRepo = repo, usuario = admin)

        vm.solicitudes.first { it != null }?.size shouldBe 2
    }

    "el técnico solo ve las solicitudes que tiene asignadas" {
        val repo = repoSolicitudes()
        every { repo.obtenerPorTecnico(3) } returns flowOf(listOf(solicitud(4, tecnicoId = 3)))
        val vm = crearViewModel(solicitudRepo = repo, usuario = tecnico)

        vm.solicitudes.first { it != null } shouldBe listOf(solicitud(4, tecnicoId = 3))
        verify(exactly = 0) { repo.obtenerTodas() }
    }

    "sin sesión iniciada la lista queda vacía" {
        val vm = crearViewModel(usuario = null)

        vm.solicitudes.first { it != null } shouldBe emptyList()
    }

    // ---------- FORMULARIO + API ----------

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

    // ---------- GUARDAR / ELIMINAR ----------

    "guardar un formulario vacío muestra errores y no guarda en Room" {
        val repo = repoSolicitudes()
        val vm = crearViewModel(solicitudRepo = repo)

        vm.guardar()

        vm.uiState.value.tituloError.shouldNotBeNull()
        vm.uiState.value.guardadoExitosamente shouldBe false
        coVerify(exactly = 0) { repo.crear(any(), any(), any(), any(), any(), any()) }
    }

    "guardar un formulario válido crea la solicitud a nombre del cliente de la sesión" {
        val repo = repoSolicitudes()
        coEvery { repo.crear(any(), any(), any(), any(), any(), any()) } returns "SOL-0007"
        val vm = crearViewModel(solicitudRepo = repo, usuario = cliente)

        vm.onTituloChange("Sitio web caído")
        vm.onDescripcionChange("El sitio de la empresa no carga desde la mañana")
        vm.onCategoriaChange("Hosting")
        vm.onServicioSeleccionado(hosting)
        vm.guardar()

        vm.uiState.value.errorGeneral.shouldBeNull()
        vm.uiState.value.guardadoExitosamente shouldBe true
        vm.uiState.value.codigo shouldBe "SOL-0007"
        // El último parámetro es el cliente: debe ser el usuario que inició sesión
        coVerify { repo.crear(any(), any(), any(), any(), any(), cliente) }
    }

    "eliminar le pide al repositorio borrar la solicitud" {
        val repo = repoSolicitudes()
        coEvery { repo.eliminar(3) } returns Unit
        val vm = crearViewModel(solicitudRepo = repo)

        vm.eliminar(3)

        coVerify(exactly = 1) { repo.eliminar(3) }
    }
})
