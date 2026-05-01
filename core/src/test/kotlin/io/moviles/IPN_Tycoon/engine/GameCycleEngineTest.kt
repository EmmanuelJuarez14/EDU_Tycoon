package io.moviles.IPN_Tycoon.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class GameCycleEngineTest {

    @Test
    fun `test advanceCycle increases cycle count`() {
        val engine = GameCycleEngine()
        assertEquals(1, engine.currentCycle.value)

        engine.advanceCycle()
        assertEquals(2, engine.currentCycle.value)
    }

    @Test
    fun `test listeners are notified in priority order`() {
        val engine = GameCycleEngine()
        val log = mutableListOf<String>()

        // Registramos un listener de EVENTOS (Prioridad baja)
        engine.addListener(object : GameCycleListener {
            override val resolutionOrder = ResolutionOrder.EVENTS
            override fun onResolveCycle(cycle: Int) {
                log.add("EVENTS")
            }
        })

        // Registramos un listener de ECONOMIA (Prioridad alta)
        engine.addListener(object : GameCycleListener {
            override val resolutionOrder = ResolutionOrder.ECONOMY
            override fun onResolveCycle(cycle: Int) {
                log.add("ECONOMY")
            }
        })

        // Al avanzar el ciclo, ECONOMIA debe ejecutarse antes que EVENTS
        engine.advanceCycle()

        assertEquals(listOf("ECONOMY", "EVENTS"), log)
    }

    @Test
    fun `test onCycleStarted is called after increment`() {
        val engine = GameCycleEngine()
        var cycleAtStart = 0

        engine.addListener(object : GameCycleListener {
            override val resolutionOrder = ResolutionOrder.ECONOMY
            override fun onCycleStarted(cycle: Int) {
                cycleAtStart = cycle
            }
            override fun onResolveCycle(cycle: Int) {}
        })

        engine.advanceCycle()

        // El ciclo debe ser 2 cuando onCycleStarted se ejecute
        assertEquals(2, cycleAtStart)
    }
}
