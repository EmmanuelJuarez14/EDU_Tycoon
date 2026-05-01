package io.moviles.IPN_Tycoon.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Motor central de ciclos del juego. Gestiona la secuencia de acciones y resolución.
 */
class GameCycleEngine {
    private val _currentCycle = MutableStateFlow(1)
    val currentCycle: StateFlow<Int> = _currentCycle.asStateFlow()

    private val listeners = mutableListOf<GameCycleListener>()

    /**
     * Registra un sistema en el motor del ciclo.
     * Automáticamente lo ordena según su prioridad de resolución.
     */
    fun addListener(listener: GameCycleListener) {
        listeners.add(listener)
        listeners.sortBy { it.resolutionOrder.ordinal }
    }

    /**
     * Avanza al siguiente ciclo.
     * Ejecuta primero la resolución de consecuencias y luego inicia el nuevo ciclo.
     */
    fun advanceCycle() {
        val cycle = _currentCycle.value

        // 1. Fase de Resolución (Cálculos de economía, estudiantes, etc.)
        listeners.forEach { it.onResolveCycle(cycle) }

        // 2. Incrementar el contador de ciclos
        _currentCycle.value += 1

        // 3. Fase de Inicio del nuevo ciclo (Planificación del jugador)
        listeners.forEach { it.onCycleStarted(_currentCycle.value) }
    }

    fun reset() {
        _currentCycle.value = 1
    }
}
