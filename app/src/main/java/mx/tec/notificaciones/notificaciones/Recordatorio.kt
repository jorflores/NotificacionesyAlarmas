package mx.tec.notificaciones.notificaciones

import android.content.Context
import android.text.format.DateFormat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.Date
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
/** Programa el aviso diario y regresa cuándo será el próximo (en milisegundos). */
fun programarRecordatorio(context: Context, hora: Int, minuto: Int): Long {
    // ¿Cuánto falta para la próxima vez que sea esa hora?
    val ahora = Calendar.getInstance()
    val inicioDeEsteMinuto = (ahora.clone() as Calendar).apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val objetivo = (inicioDeEsteMinuto.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        // Ya pasó hoy: mañana. Si es el minuto actual (12:16 y son 12:16:40), cuenta como hoy.
        if (before(inicioDeEsteMinuto)) add(Calendar.DAY_OF_MONTH, 1)
    }
    val espera = maxOf(0L, objetivo.timeInMillis - ahora.timeInMillis)

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
    return objetivo.timeInMillis
}

/** "8:00 a. m." o "08:00", según el formato de hora que use el teléfono. */
fun formatearHora(context: Context, millis: Long): String =
    DateFormat.getTimeFormat(context).format(Date(millis))

fun formatearHora(context: Context, hora: Int, minuto: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
    }
    return formatearHora(context, cal.timeInMillis)
}

/** "hoy" o "mañana", para avisarle al usuario cuándo llegará. */
fun diaRelativo(millis: Long): String {
    val hoy = Calendar.getInstance()
    val dia = Calendar.getInstance().apply { timeInMillis = millis }
    val mismoDia = hoy.get(Calendar.YEAR) == dia.get(Calendar.YEAR) &&
        hoy.get(Calendar.DAY_OF_YEAR) == dia.get(Calendar.DAY_OF_YEAR)
    return if (mismoDia) "hoy" else "mañana"
}

fun cancelarRecordatorio(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(TRABAJO_DIARIO)
}
