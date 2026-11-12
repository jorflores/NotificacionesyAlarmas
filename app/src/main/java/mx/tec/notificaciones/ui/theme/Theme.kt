package mx.tec.notificaciones.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Verde = Color(0xFF17834A)
private val VerdeAndroid = Color(0xFF3DDC84)

private val Claro = lightColorScheme(
    primary = Verde,
    secondary = Color(0xFF073042),
)

private val Oscuro = darkColorScheme(
    primary = VerdeAndroid,
    secondary = Color(0xFF9FB7C2),
)

@Composable
fun NotificacionesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        content = content
    )
}
