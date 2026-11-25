package com.example.solitrack.ui

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.solitrack.data.local.AppDatabase
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.Rol
import com.example.solitrack.data.model.Servicio
import com.example.solitrack.data.remote.ApiService
import com.example.solitrack.data.repository.ServicioRepository
import com.example.solitrack.data.repository.SesionRepository
import com.example.solitrack.data.repository.SolicitudRepository
import com.example.solitrack.ui.solicitud.ListaSolicitudesScreen
import com.example.solitrack.ui.solicitud.SolicitudViewModel
import com.example.solitrack.ui.theme.SoliTrackTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

// Test de interfaz: se ejecuta en el emulador y "toca" la pantalla como un usuario.
// Usa una base de datos Room en memoria (se borra al terminar) para no tocar los datos reales.
@RunWith(AndroidJUnit4::class)
class ListaSolicitudesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var repository: SolicitudRepository

    // La lista no usa la API, así que basta con una implementación vacía
    private val apiFalsa = object : ApiService {
        override suspend fun obtenerServicios(): List<Servicio> = emptyList()
        override suspend fun crearServicio(servicio: Servicio) = servicio
        override suspend fun actualizarServicio(id: Int, servicio: Servicio) = servicio
        override suspend fun eliminarServicio(id: Int): Response<Unit> = Response.success(Unit)
    }

    // Sesión iniciada como cliente: ve sus solicitudes, puede crear y eliminar
    private val sesionCliente = SesionRepository().apply {
        iniciar(UsuarioEntity(id = 1, usuario = "cliente", clave = "1234", nombre = "Camila Rojas", rol = Rol.CLIENTE))
    }

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext

        // La lista pide permiso de notificaciones al abrirse (Android 13+).
        // Se concede antes para que el diálogo del sistema no tape la pantalla.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = SolicitudRepository(db.solicitudDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun mostrarPantalla() {
        val viewModel = SolicitudViewModel(repository, ServicioRepository(apiFalsa), sesionCliente)
        composeRule.setContent {
            SoliTrackTheme {
                ListaSolicitudesScreen(
                    onNuevaSolicitud = {},
                    onAbrirSolicitud = {},
                    onVolver = {},
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun sinSolicitudes_muestraMensajeDeListaVacia() {
        mostrarPantalla()

        composeRule.onNodeWithText("Aún no hay solicitudes").assertIsDisplayed()
    }

    @Test
    fun conSolicitudes_muestraCodigoYTitulo() {
        runBlocking {
            repository.crear("Sitio web caído", "No carga desde la mañana", "Hosting", "Hosting básico", null, clienteId = 1)
        }

        mostrarPantalla()

        composeRule.onNodeWithText("SOL-0001").assertIsDisplayed()
        composeRule.onNodeWithText("Sitio web caído").assertIsDisplayed()
        composeRule.onNodeWithText("1 solicitud(es) registrada(s)").assertIsDisplayed()
    }

    @Test
    fun eliminar_confirmandoElDialogo_quitaLaSolicitudDeLaLista() {
        runBlocking {
            repository.crear("Cambio de logo", "Actualizar el logo del sitio", "Solicitud de cambio", "Cambio de diseño", null, clienteId = 1)
        }
        mostrarPantalla()

        // Toca el basurero de la tarjeta y confirma en el diálogo
        composeRule.onNodeWithContentDescription("Eliminar SOL-0001").performClick()
        composeRule.onNodeWithText("Eliminar solicitud").assertIsDisplayed()
        composeRule.onNodeWithText("Eliminar").performClick()

        // Room emite la lista vacía y la pantalla vuelve al estado vacío
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("Aún no hay solicitudes"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Aún no hay solicitudes").assertIsDisplayed()
    }
}
