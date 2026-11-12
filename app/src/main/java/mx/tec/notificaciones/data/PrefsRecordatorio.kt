package mx.tec.notificaciones.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Igual que en la clase de DataStore: una sola instancia, a nivel superior del archivo
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recordatorio_prefs")

private object Claves {
    val HABILITADO = booleanPreferencesKey("habilitado")
    val HORA = intPreferencesKey("hora")
    val MINUTO = intPreferencesKey("minuto")
}

/** Lo que el usuario eligió para el recordatorio diario. */
data class ConfigRecordatorio(
    val habilitado: Boolean = false,
    val hora: Int = 8,
    val minuto: Int = 0
)

/**
 * Guarda la configuración para que el switch y la hora sigan igual
 * al cerrar y volver a abrir la app.
 */
class PrefsRecordatorio(private val context: Context) {

    val config: Flow<ConfigRecordatorio> = context.dataStore.data.map { p ->
        ConfigRecordatorio(
            habilitado = p[Claves.HABILITADO] ?: false,
            hora = p[Claves.HORA] ?: 8,
            minuto = p[Claves.MINUTO] ?: 0
        )
    }

    suspend fun guardarHabilitado(habilitado: Boolean) {
        context.dataStore.edit { it[Claves.HABILITADO] = habilitado }
    }

    suspend fun guardarHora(hora: Int, minuto: Int) {
        context.dataStore.edit { p ->
            p[Claves.HORA] = hora
            p[Claves.MINUTO] = minuto
        }
    }
}
