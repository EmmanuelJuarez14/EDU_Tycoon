package io.moviles.IPN_Tycoon.viewmodels

import io.moviles.IPN_Tycoon.GameState
import io.moviles.IPN_Tycoon.PropiedadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel de estado del juego.
 * Expone flows observables calculados desde GameState y PropiedadRepository.
 * Se actualiza manualmente llamando a [refresh] después de cada ciclo o acción.
 */
class GameStateViewModel {

    private val _dinero = MutableStateFlow(GameState.dinero)
    val dinero = _dinero.asStateFlow()

    private val _ciclos = MutableStateFlow(GameState.ciclosJugados)
    val ciclos = _ciclos.asStateFlow()

    private val _alumnosTotales = MutableStateFlow(calcularAlumnos())
    val alumnosTotales = _alumnosTotales.asStateFlow()

    private val _edificiosComprados = MutableStateFlow(contarEdificios())
    val edificiosComprados = _edificiosComprados.asStateFlow()

    /** Sincroniza todos los flows con el estado actual. */
    fun refresh() {
        _dinero.value           = GameState.dinero
        _ciclos.value           = GameState.ciclosJugados
        _alumnosTotales.value   = calcularAlumnos()
        _edificiosComprados.value = contarEdificios()
    }

    private fun calcularAlumnos(): Int =
        PropiedadRepository.propiedades.values
            .filter { it.comprada }
            .sumOf { it.baseAlumnos * it.nivel }

    private fun contarEdificios(): Int =
        PropiedadRepository.propiedades.values.count { it.comprada }
}
