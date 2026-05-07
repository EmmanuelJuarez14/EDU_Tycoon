package io.moviles.IPN_Tycoon

/**
 * Estado global del juego. Aquí vive el dinero del jugador.
 * Cuando integres Room, este objeto se sincronizará con la BD
 * a través de EconomyEngine / GameCycleEngine.
 */
object GameState {
    private const val DINERO_INICIAL: Long = 500_000L
    var dinero: Long = DINERO_INICIAL   // Saldo inicial

    fun reset() {
        dinero = DINERO_INICIAL
    }

    fun reiniciar() = reset()

    /** Retorna true si el jugador tiene fondos suficientes. */
    fun puedeComprar(costo: Long): Boolean = dinero >= costo

    /**
     * Descuenta [cantidad] del saldo.
     * @return true si la transacción fue exitosa.
     */
    fun gastar(cantidad: Long): Boolean {
        if (dinero < cantidad) return false
        dinero -= cantidad
        return true
    }

    /** Usado por EconomyEngine para acreditar ingresos por ciclo. */
    fun acreditar(cantidad: Long) {
        dinero += cantidad
    }

    /** Costo de mejora escalado: precio_base × nivel_actual. */
    fun costoMejora(propiedad: Propiedad): Long =
        propiedad.precio * propiedad.nivel
}
