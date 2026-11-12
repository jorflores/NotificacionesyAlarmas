package mx.tec.notificaciones

/**
 * ¿El usuario está viendo la app en este momento?
 *
 * MainActivity lo actualiza en onStart() (se ve) y onStop() (ya no se ve).
 * Sirve para decidir cómo avisar: Toast si está en la app, notificación si no.
 */
object EstadoApp {
    @Volatile
    var enPantalla: Boolean = false
}
