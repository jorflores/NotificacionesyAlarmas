package mx.tec.notificaciones.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import mx.tec.notificaciones.MainActivity
import mx.tec.notificaciones.R

// Ids que usamos en la app. Con el mismo id, una notificación nueva reemplaza a la anterior.
const val CANAL_RECORDATORIOS = "recordatorios"
const val ID_INMEDIATA = 1
const val ID_PRUEBA = 2
const val ID_DIARIA = 3

// ---------------------------------------------------------------------------
// Paso 1 · Preparar: crear el canal (se llama en MainActivity.onCreate)
// ---------------------------------------------------------------------------
fun crearCanal(context: Context) {
    // Los canales existen desde Android 8 (API 26). Como el minSdk de este
    // proyecto ya es 26, el if siempre se cumple; lo dejamos igual que en la
    // presentación para que se vea la regla.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            CANAL_RECORDATORIOS,                    // id interno
            "Recordatorios",                        // nombre que se ve en Ajustes
            NotificationManager.IMPORTANCE_DEFAULT  // suena, sin aparecer encima
        ).apply { description = "Avisos de la app de ejemplo" }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(canal)
    }
}

// ---------------------------------------------------------------------------
// Paso 2 · ¿Ya tenemos permiso?
// ---------------------------------------------------------------------------
fun tienePermiso(context: Context): Boolean {
    // Antes de Android 13 este permiso no existe: las notificaciones se permiten por defecto
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return true
    }
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

// ---------------------------------------------------------------------------
// Paso 3 · Construir y mostrar
// ---------------------------------------------------------------------------
@SuppressLint("MissingPermission") // revisado con tienePermiso()
fun mostrarNotificacion(
    context: Context,
    titulo: String,
    texto: String,
    id: Int = ID_INMEDIATA
) {
    if (!tienePermiso(context)) return

    // Al tocarla, abrir la app
    val abrirApp = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )

    val notificacion = NotificationCompat
        .Builder(context, CANAL_RECORDATORIOS)
        .setSmallIcon(R.drawable.ic_notificacion)  // obligatorio
        .setContentTitle(titulo)
        .setContentText(texto)
        .setContentIntent(abrirApp)
        .setAutoCancel(true)                       // se quita al tocarla
        .build()

    NotificationManagerCompat.from(context).notify(id, notificacion)
}
