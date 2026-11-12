package mx.tec.notificaciones.notificaciones

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * Trabajo que Android ejecuta cuando toca, aunque la app esté cerrada.
 * Solo muestra una notificación: reutiliza mostrarNotificacion() del Paso 3.
 *
 * El título, el texto y el id llegan en inputData, así el mismo Worker sirve
 * para la prueba de 10 segundos y para el recordatorio diario.
 */
class RecordatorioWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        mostrarNotificacion(
            applicationContext,
            inputData.getString(CLAVE_TITULO) ?: "Recordatorio",
            inputData.getString(CLAVE_TEXTO) ?: "",
            inputData.getInt(CLAVE_ID, ID_DIARIA)
        )
        return Result.success()
    }

    companion object {
        private const val CLAVE_TITULO = "titulo"
        private const val CLAVE_TEXTO = "texto"
        private const val CLAVE_ID = "id"

        /** Arma los datos que recibe el Worker. */
        fun datos(titulo: String, texto: String, id: Int): Data =
            workDataOf(CLAVE_TITULO to titulo, CLAVE_TEXTO to texto, CLAVE_ID to id)
    }
}
