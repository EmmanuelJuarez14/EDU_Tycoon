package io.moviles.IPN_Tycoon.engine

import io.moviles.IPN_Tycoon.GameState
import io.moviles.IPN_Tycoon.PropiedadRepository

/**
 * Sistema de economía por ciclo.
 * Lee el estado de edificios directamente desde PropiedadRepository (in-memory)
 * y actualiza GameState.dinero — Room se sincroniza solo al guardar partida.
 */
class EconomyEngine : GameCycleListener {

    override val resolutionOrder = ResolutionOrder.ECONOMY

    private val INGRESO_POR_ALUMNO     = 10L
    private val GASTO_BASE_POR_NIVEL   = 500L

    override fun onResolveCycle(cycle: Int) {
        val propiedadesActivas = PropiedadRepository.propiedades.values.filter { it.comprada }

        var ingresosTotales = 0L
        var gastosTotales   = 0L

        propiedadesActivas.forEach { propiedad ->
            ingresosTotales += propiedad.baseAlumnos * propiedad.nivel * INGRESO_POR_ALUMNO
            gastosTotales   += propiedad.nivel * GASTO_BASE_POR_NIVEL
        }

        val balanceNeto = ingresosTotales - gastosTotales
        GameState.acreditar(balanceNeto)
        GameState.ciclosJugados++
    }
}
