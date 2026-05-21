package io.moviles.IPN_Tycoon.engine

import io.moviles.IPN_Tycoon.GameState
import io.moviles.IPN_Tycoon.PropiedadRepository

/**
 * Recalcula alumnosTotales en GameState al final de cada ciclo.
 * Corre después de EconomyEngine (ResolutionOrder.STUDENTS).
 */
class EstudiantesEngine : GameCycleListener {

    override val resolutionOrder = ResolutionOrder.STUDENTS

    override fun onResolveCycle(cycle: Int) {
        GameState.alumnosTotales = PropiedadRepository.propiedades.values
            .filter { it.comprada }
            .sumOf { it.baseAlumnos * it.nivel }
    }
}
