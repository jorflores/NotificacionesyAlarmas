# Notificaciones y Alarmas

App de ejemplo de la clase **Toasts y notificaciones** (TC2007B · Módulo 3 · Desarrollo Móvil).

Una sola pantalla con cuatro tarjetas:

| # | Ejemplo | Qué muestra |
|---|---------|-------------|
| 1 | **Toast** | Aviso rápido dentro de la app (`LENGTH_SHORT` y `LENGTH_LONG`). No necesita permiso. |
| 2 | **Notificación inmediata** | Canal + permiso + `NotificationCompat.Builder` + `notify()`. Al tocarla abre la app. |
| 3 | **Notificación en 10 segundos** | La programa WorkManager. Cierra la app y espera: llega igual. |
| 4 | **Recordatorio diario** | El usuario elige la hora y lo activa o desactiva con un switch. Se guarda con DataStore y se programa con WorkManager. |

## Estructura

```
app/src/main/java/mx/tec/notificaciones/
├── MainActivity.kt                 crea el canal y muestra la pantalla
├── notificaciones/
│   ├── Notificaciones.kt           Pasos 1–3: crearCanal(), tienePermiso(), mostrarNotificacion()
│   ├── RecordatorioWorker.kt       Worker que muestra la notificación
│   └── Recordatorio.kt             programarPrueba(), programarRecordatorio(), cancelarRecordatorio()
├── data/
│   └── PrefsRecordatorio.kt        DataStore: activo, hora y minuto
└── ui/
    ├── NotificacionesScreen.kt     Paso 4: pedir permiso y las cuatro tarjetas
    ├── RecordatorioViewModel.kt    une DataStore (lo que eligió el usuario) con WorkManager
    └── theme/Theme.kt
```

El permiso `POST_NOTIFICATIONS` está en `AndroidManifest.xml`.

## Relación con la presentación

| Diapositiva | Archivo |
|-------------|---------|
| Toast | `ui/NotificacionesScreen.kt` (tarjeta 1) |
| Paso 1 · Preparar: permiso y canal | `AndroidManifest.xml`, `crearCanal()` en `Notificaciones.kt` |
| Paso 2 · ¿Ya tenemos permiso? | `tienePermiso()` en `Notificaciones.kt` |
| Paso 3 · Construir y mostrar | `mostrarNotificacion()` en `Notificaciones.kt` |
| Paso 4 · Pedir permiso desde la pantalla | `conPermiso { }` en `NotificacionesScreen.kt` |
| Avisar más tarde con WorkManager | `RecordatorioWorker.kt`, `programarPrueba()` |
| Opcional: recordatorio para tomar lista | `programarRecordatorio()`, `PrefsRecordatorio.kt`, `RecordatorioViewModel.kt` |

Diferencias pequeñas con las diapositivas:

- `mostrarNotificacion()` recibe un `id`. Cada ejemplo usa uno distinto para que no se reemplacen entre sí.
- La pantalla usa `conPermiso { … }`, que guarda la acción y la ejecuta cuando el usuario acepta el permiso. Así sirve igual para los tres botones y para el switch.
- El Worker recibe título, texto e id por `inputData`, así el mismo Worker sirve para la prueba y para el diario.
- El recordatorio diario usa `ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE`: al cambiar la hora se reprograma. Con `KEEP` se quedaría la hora anterior.

## Cómo probarlo

1. Usa un emulador o teléfono con **Android 13 o más nuevo** para ver el diálogo del permiso.
2. **Toast:** toca *Corto* y *Largo*.
3. **Inmediata:** toca *Enviar ahora*, acepta el permiso y baja la barra de notificaciones. Tócala: abre la app.
4. **10 segundos:** toca *Programar*, cierra la app desde recientes y espera.
5. **Diario:** elige una hora 2 o 3 minutos adelante y activa el switch. Puede llegar unos minutos tarde.
   - Para ver el trabajo programado: **App Inspection > Background Task Inspector** en Android Studio.
   - Cierra y vuelve a abrir la app: el switch y la hora siguen igual (DataStore).

Para repetir la prueba del permiso: **Ajustes > Apps > Notificaciones > Notificaciones** y desactívalas, o desinstala la app.

## Notas

- **La hora del recordatorio diario es aproximada.** WorkManager puede retrasarla para ahorrar batería y el periodo de 24 h se puede desfasar. Para hora exacta existe `AlarmManager`, pero desde Android 12 pide un permiso especial y en Android 14 ya no se concede por defecto a la mayoría de las apps.
- `minSdk` es 26, así que el `if` de versión en `crearCanal()` siempre se cumple. Se deja para que coincida con la presentación; Android Studio lo marca con una advertencia.
- Configuración de Gradle igual a la de [AppBase](https://github.com/jorflores/AppBase), más WorkManager 2.12.0 y DataStore 1.3.0.
