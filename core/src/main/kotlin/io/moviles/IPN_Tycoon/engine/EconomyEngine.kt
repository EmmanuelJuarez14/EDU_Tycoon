package io.moviles.IPN_Tycoon.engine

import io.moviles.IPN_Tycoon.GameState
import io.moviles.IPN_Tycoon.PropiedadRepository

/**
 * Calcula ingresos pasivos de los edificios comprados cada ciclo.
 * Los efectos de eventos se resuelven en el mismo ciclo mediante EventEngine.
 * Payback objetivo: ~10 ciclos por edificio básico.
 */
class EconomyEngine : GameCycleListener {

    override val resolutionOrder = ResolutionOrder.ECONOMY

    private val INGRESO_POR_ALUMNO = 50L   // era 10 → payback ~10 ciclos para edificios básicos

    data class CycleResult(val ingresos: Long)

    var lastResult: CycleResult? = null
        private set

    override fun onResolveCycle(cycle: Int) {
        val ingresosTotales = PropiedadRepository.propiedades.values
            .filter { it.comprada }
            .sumOf { it.baseAlumnos * it.nivel * INGRESO_POR_ALUMNO }

        lastResult = CycleResult(ingresosTotales)
        GameState.acreditar(ingresosTotales)
        GameState.ciclosJugados++
    }
}
