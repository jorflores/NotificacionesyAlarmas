package mx.tec.notificaciones.notificaciones

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val TRABAJO_DIARIO = "recordatorio_diario"

// ---------------------------------------------------------------------------
// Avisar más tarde: una sola vez, dentro de 10 segundos
// ---------------------------------------------------------------------------
fun programarPrueba(context: Context) {
    val prueba = OneTimeWorkRequestBuilder<RecordatorioWorker>()
        .setInitialDelay(10, TimeUnit.SECONDS)
        .setInputData(
            RecordatorioWorker.datos(
                "Pasaron 10 segundos",
                "Esta notificación la programó WorkManager",
                ID_PRUEBA
            )
        )
        .build()

    WorkManager.getInstance(context).enqueue(prueba)
}

// ---------------------------------------------------------------------------
// Opcional: recordatorio diario a una hora elegida por el usuario
// ---------------------------------------------------------------------------
fun programarRecordatorio(context: Context, hora: Int, minuto: Int) {
    // ¿Cuánto falta para la próxima vez que sea esa hora?
    val ahora = Calendar.getInstance()
    val objetivo = (ahora.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(ahora)) add(Calendar.DAY_OF_MONTH, 1)  // ya pasó hoy: mañana
    }
    val espera = objetivo.timeInMillis - ahora.timeInMillis

    val diario = PeriodicWorkRequestBuilder<RecordatorioWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(espera, TimeUnit.MILLISECONDS)
        .setInputData(
            RecordatorioWorker.datos(
                "¿Ya tomaste lista?",
                "Registra la asistencia de hoy",
                ID_DIARIA
            )
        )
        .build()

    // Un nombre único: si ya había uno programado, lo cancela y lo vuelve a
    // programar con la hora nueva (con KEEP se quedaría la hora vieja).
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        TRABAJO_DIARIO,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        diario
    )
}

fun cancelarRecordatorio(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(TRABAJO_DIARIO)
}
