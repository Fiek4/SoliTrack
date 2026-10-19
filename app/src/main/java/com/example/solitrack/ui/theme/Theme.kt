package com.example.solitrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AzulSoliTrack,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = TurquesaSoliTrack,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = AmbarSoliTrack,
    onTertiary = Color.Black,
    background = FondoClaro,
    surface = SuperficieClaro,
    surfaceVariant = SuperficieVarianteClaro,
    surfaceContainerLow = SuperficieClaro,
    surfaceContainer = ContenedorClaro,
    surfaceContainerHigh = ContenedorAltoClaro
)

private val DarkColors = darkColorScheme(
    primary = AzulSoliTrackClaro,
    onPrimary = Color(0xFF002F65),
    primaryContainer = Color(0xFF00458E),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = TurquesaSoliTrackClaro,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = AmbarSoliTrackClaro,
    onTertiary = Color.Black,
    background = FondoOscuro,
    surface = SuperficieOscuro,
    surfaceVariant = SuperficieVarianteOscuro,
    surfaceContainerLow = SuperficieOscuro,
    surfaceContainer = ContenedorOscuro,
    surfaceContainerHigh = ContenedorAltoOscuro
)

@Composable
fun SoliTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
