package mx.tec.notificaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.tec.notificaciones.notificaciones.crearCanal
import mx.tec.notificaciones.ui.NotificacionesScreen
import mx.tec.notificaciones.ui.theme.NotificacionesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Paso 1: crear el canal antes de mostrar cualquier notificación.
        // Llamarlo cada vez que abre la app no hace daño.
        crearCanal(this)

        enableEdgeToEdge()
        setContent {
            NotificacionesTheme {
                NotificacionesScreen()
            }
        }
    }
}
