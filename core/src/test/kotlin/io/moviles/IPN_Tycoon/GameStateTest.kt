package io.moviles.IPN_Tycoon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameStateTest {

    @Before
    fun setUp() {
        GameState.reset()
    }

    @Test
    fun `puedeComprar returns true only when funds are enough`() {
        assertTrue(GameState.puedeComprar(500_000L))
        assertFalse(GameState.puedeComprar(500_001L))
    }

    @Test
    fun `gastar discounts money only on successful purchase`() {
        assertTrue(GameState.gastar(100_000L))
        assertEquals(400_000L, GameState.dinero)

        assertFalse(GameState.gastar(500_000L))
        assertEquals(400_000L, GameState.dinero)
    }

    @Test
    fun `costoMejora uses current level and base price`() {
        val propiedad = Propiedad(
            id = "test",
            nombre = "Test",
            precio = 10_000L,
            descripcion = "test",
            capacidad = 1,
            baseAlumnos = 1,
            mejoraMax = 3,
            nivel = 2
        )

        assertEquals(20_000L, GameState.costoMejora(propiedad))
    }
}
