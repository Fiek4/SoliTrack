package com.example.solitrack.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.solitrack.ui.login.LoginScreen
import com.example.solitrack.ui.menu.MenuScreen
import com.example.solitrack.ui.solicitud.ListaSolicitudesScreen
import com.example.solitrack.ui.solicitud.SolicitudFormScreen

object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu/{usuario}"
    const val LISTA = "solicitudes"
    const val FORMULARIO = "formulario?id={id}"

    fun menu(usuario: String) = "menu/${Uri.encode(usuario)}"

    fun formulario(id: Long? = null) =
        if (id == null) "formulario" else "formulario?id=$id"
}

private const val DURACION_ANIMACION = 350
private const val SIN_ID = -1L

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        // ---------- Animaciones de transición ----------
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(DURACION_ANIMACION)
            ) + fadeIn(tween(DURACION_ANIMACION))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(DURACION_ANIMACION)
            ) + fadeOut(tween(DURACION_ANIMACION))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(DURACION_ANIMACION)
            ) + fadeIn(tween(DURACION_ANIMACION))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(DURACION_ANIMACION)
            ) + fadeOut(tween(DURACION_ANIMACION))
        }
    ) {

        // ---------- LOGIN ----------
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = { usuario ->
                    navController.navigate(Rutas.menu(usuario)) {
                        // Al presionar "atrás" en el menú no se vuelve al login
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ---------- MENÚ ----------
        composable(
            route = Rutas.MENU,
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            MenuScreen(
                usuario = backStackEntry.arguments?.getString("usuario").orEmpty(),
                onIrASolicitudes = { navController.navigate(Rutas.LISTA) },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        // Borra todo el historial de navegación
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // ---------- LISTA DE SOLICITUDES ----------
        composable(Rutas.LISTA) {
            ListaSolicitudesScreen(
                onNuevaSolicitud = { navController.navigate(Rutas.formulario()) },
                onAbrirSolicitud = { id -> navController.navigate(Rutas.formulario(id)) },
                onVolver = { navController.volverDesde(Rutas.LISTA) }
            )
        }

        // ---------- FORMULARIO (crear / editar) ----------
        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = SIN_ID
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it != SIN_ID }

            SolicitudFormScreen(
                solicitudId = id,
                onVolver = { navController.volverDesde(Rutas.FORMULARIO) }
            )
        }
    }
}

// Vuelve atrás solo si la pantalla actual es "ruta".
// Evita que un doble toque (o volver justo al terminar de guardar)
// saque al usuario de más de una pantalla.
private fun NavController.volverDesde(ruta: String) {
    if (currentBackStackEntry?.destination?.route == ruta) {
        popBackStack()
    }
}
