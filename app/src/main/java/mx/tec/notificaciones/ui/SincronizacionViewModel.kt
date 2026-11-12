package mx.tec.notificaciones.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.notificaciones.EstadoApp
import mx.tec.notificaciones.notificaciones.ID_INMEDIATA
import mx.tec.notificaciones.notificaciones.mostrarNotificacion

/**
 * Simula enviar a la API las asistencias guardadas sin internet.
 *
 * Al terminar elige cómo avisar:
 * - El usuario sigue en la app        → Toast.
 * - El usuario se fue a otra app      → notificación inmediata.
 *
 * Nota: el trabajo vive en viewModelScope. Si el usuario quita la app de
 * recientes, se cancela. Para una sincronización real usarías un Worker,
 * que sigue aunque la app se cierre.
 */
class SincronizacionViewModel(app: Application) : AndroidViewModel(app) {

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    fun sincronizar() {
        if (_sincronizando.value) return  // ya hay una en curso

        viewModelScope.launch {
            _sincronizando.value = true
            delay(5_000)  // aquí iría la llamada a la API con Retrofit
            _sincronizando.value = false

            val enviadas = 12
            avisarFin(enviadas)
        }
    }

    private fun avisarFin(enviadas: Int) {
        val app = getApplication<Application>()
        if (EstadoApp.enPantalla) {
            Toast.makeText(app, "Se enviaron $enviadas asistencias", Toast.LENGTH_SHORT).show()
        } else {
            mostrarNotificacion(
                app,
                "Sincronización completa",
                "Se enviaron $enviadas asistencias",
                ID_INMEDIATA
            )
        }
    }
}
