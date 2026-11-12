package mx.tec.notificaciones.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.tec.notificaciones.data.ConfigRecordatorio
import mx.tec.notificaciones.data.PrefsRecordatorio
import mx.tec.notificaciones.notificaciones.cancelarRecordatorio
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
            programarRecordatorio(getApplication(), actual.hora, actual.minuto)
        } else {
            cancelarRecordatorio(getApplication())
        }
    }

    fun cambiarHora(hora: Int, minuto: Int) = viewModelScope.launch {
        prefs.guardarHora(hora, minuto)
        // Si ya estaba activo, se vuelve a programar con la hora nueva
        if (config.value.habilitado) {
            programarRecordatorio(getApplication(), hora, minuto)
        }
    }
}
