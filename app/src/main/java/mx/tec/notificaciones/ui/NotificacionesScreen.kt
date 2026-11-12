package mx.tec.notificaciones.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.tec.notificaciones.notificaciones.programarPrueba
import mx.tec.notificaciones.notificaciones.tienePermiso

@Composable
fun NotificacionesScreen(
    vm: RecordatorioViewModel = viewModel(),
    syncVm: SincronizacionViewModel = viewModel()
) {
    val context = LocalContext.current
    val config by vm.config.collectAsStateWithLifecycle()
    val sincronizando by syncVm.sincronizando.collectAsStateWithLifecycle()
    var mostrarDialogoHora by remember { mutableStateOf(false) }

    // ---- Paso 4 · Pedir permiso desde la pantalla ----
    // Guardamos lo que el usuario quería hacer para ejecutarlo si acepta el permiso.
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            accionPendiente?.invoke()
        } else {
            Toast.makeText(
                context, "Sin permiso no se pueden mostrar notificaciones", Toast.LENGTH_LONG
            ).show()
        }
        accionPendiente = null
    }

    // Si ya hay permiso, hace la acción; si no, primero lo pide.
    fun conPermiso(accion: () -> Unit) {
        if (tienePermiso(context)) {
            accion()
        } else {
            accionPendiente = accion
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Toasts y notificaciones", style = MaterialTheme.typography.headlineMedium)

            // 1. Toast: aviso dentro de la app
            Seccion(
                icono = Icons.Filled.Info,
                titulo = "1. Toast",
                descripcion = "Aviso rápido dentro de la app. No necesita permiso."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        Toast.makeText(context, "Asistencia guardada", Toast.LENGTH_SHORT).show()
                    }) { Text("Corto") }

                    OutlinedButton(onClick = {
                        Toast.makeText(
                            context, "Este Toast dura un poco más", Toast.LENGTH_LONG
                        ).show()
                    }) { Text("Largo") }
                }
            }

            // 2. Notificación inmediata: la dispara un evento (terminó la sincronización)
            Seccion(
                icono = Icons.AutoMirrored.Filled.Send,
                titulo = "2. Notificación inmediata",
                descripcion = "Simula enviar asistencias a la API (5 s). Si te quedas en la app, " +
                    "ves un Toast. Si te sales, al terminar llega una notificación."
            ) {
                if (sincronizando) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Sincronizando… sal de la app para ver la notificación")
                    }
                } else {
                    Button(onClick = {
                        // Pedimos el permiso antes, por si el usuario se sale de la app
                        conPermiso { syncVm.sincronizar() }
                    }) { Text("Sincronizar asistencias") }
                }
            }

            // 3. Notificación programada en 10 segundos (WorkManager)
            Seccion(
                icono = Icons.Filled.PlayArrow,
                titulo = "3. Notificación en 10 segundos",
                descripcion = "La programa WorkManager. Cierra la app y espera: llega igual."
            ) {
                Button(onClick = {
                    conPermiso {
                        programarPrueba(context)
                        Toast.makeText(
                            context, "Programada: llega en unos 10 segundos", Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { Text("Programar") }
            }

            // 4. Recordatorio diario (opcional en el proyecto)
            Seccion(
                icono = Icons.Filled.DateRange,
                titulo = "4. Recordatorio diario",
                descripcion = "Todos los días a la hora elegida. La hora es aproximada: " +
                    "Android la puede retrasar unos minutos para ahorrar batería."
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activo", modifier = Modifier.weight(1f))
                    Switch(
                        checked = config.habilitado,
                        onCheckedChange = { activo ->
                            if (activo) conPermiso { vm.cambiarHabilitado(true) }
                            else vm.cambiarHabilitado(false)
                        }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hora", modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { mostrarDialogoHora = true }) {
                        Text("%02d:%02d".format(config.hora, config.minuto))
                    }
                }
            }
        }
    }

    if (mostrarDialogoHora) {
        DialogoHora(
            horaInicial = config.hora,
            minutoInicial = config.minuto,
            onAceptar = { hora, minuto ->
                vm.cambiarHora(hora, minuto)
                mostrarDialogoHora = false
            },
            onCancelar = { mostrarDialogoHora = false }
        )
    }
}

/** Tarjeta con ícono, título, descripción y los controles de cada ejemplo. */
@Composable
private fun Seccion(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    contenido: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(titulo, style = MaterialTheme.typography.titleMedium)
            }
            Text(descripcion, style = MaterialTheme.typography.bodyMedium)
            contenido()
        }
    }
}

/** Diálogo con el reloj de Material 3 para elegir la hora. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoHora(
    horaInicial: Int,
    minutoInicial: Int,
    onAceptar: (Int, Int) -> Unit,
    onCancelar: () -> Unit
) {
    val estado = rememberTimePickerState(
        initialHour = horaInicial,
        initialMinute = minutoInicial,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Hora del recordatorio") },
        text = { TimePicker(state = estado) },
        confirmButton = {
            TextButton(onClick = { onAceptar(estado.hour, estado.minute) }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
