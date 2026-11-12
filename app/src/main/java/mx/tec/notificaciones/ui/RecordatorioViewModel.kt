package mx.tec.notificaciones.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.tec.notificaciones.data.ConfigRecordatorio
import mx.tec.notificaciones.data.PrefsRecordatorio
import mx.tec.notificaciones.notificaciones.cancelarRecordatorio
import mx.tec.notificaciones.notificaciones.diaRelativo
import mx.tec.notificaciones.notificaciones.formatearHora
import mx.tec.notificaciones.notificaciones.programarRecordatorio

/**
 * Une las dos piezas del recordatorio diario:
 * - DataStore recuerda lo que eligió el usuario (para pintar la pantalla).
 * - WorkManager programa o cancela el aviso.
 */
class RecordatorioViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PrefsRecordatorio(app)

    val config: StateFlow<ConfigRecordatorio> = prefs.config.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConfigRecordatorio()
    )

    fun cambiarHabilitado(habilitado: Boolean) = viewModelScope.launch {
        prefs.guardarHabilitado(habilitado)
        val actual = config.value
        if (habilitado) {
            programarYAvisar(actual.hora, actual.minuto)
        } else {
            cancelarRecordatorio(getApplication())
            avisar("Recordatorio desactivado")
        }
    }

    fun cambiarHora(hora: Int, minuto: Int) = viewModelScope.launch {
        prefs.guardarHora(hora, minuto)
        // Si ya estaba activo, se vuelve a programar con la hora nueva
        if (config.value.habilitado) {
            programarYAvisar(hora, minuto)
        }
    }

    // Le confirmamos al usuario cuándo llegará, para que no haya sorpresas
    private fun programarYAvisar(hora: Int, minuto: Int) {
        val app = getApplication<Application>()
        val proximo = programarRecordatorio(app, hora, minuto)
        avisar("Próximo recordatorio: ${diaRelativo(proximo)} a las ${formatearHora(app, proximo)}")
    }

    private fun avisar(mensaje: String) {
        Toast.makeText(getApplication(), mensaje, Toast.LENGTH_LONG).show()
    }
}
