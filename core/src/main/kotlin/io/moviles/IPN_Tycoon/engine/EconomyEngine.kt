package io.moviles.IPN_Tycoon.engine

import io.moviles.IPN_Tycoon.data.local.entities.RecursoEntity
import io.moviles.IPN_Tycoon.data.repositories.EscuelaRepository
import io.moviles.IPN_Tycoon.data.repositories.RecursoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Sistema que gestiona el calculo de ingresos y gastos cada ciclo.
 */
class EconomyEngine(
    private val escuelaRepository: EscuelaRepository,
    private val recursoRepository: RecursoRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : GameCycleListener {

    override val resolutionOrder = ResolutionOrder.ECONOMY

    // Constantes de balanceo (pueden moverse a un archivo de configuracion luego)
    private val INGRESO_POR_ALUMNO = 10L
    private val GASTO_BASE_POR_NIVEL = 500L

    override fun onResolveCycle(cycle: Int) {
        scope.launch {
            // 1. Obtener todas las escuelas compradas
            val escuelas = escuelaRepository.allEscuelas.first().filter { it.comprada }

            var ingresosTotales = 0L
            var gastosTotales = 0L

            // 2. Calcular balances
            escuelas.forEach { escuela ->
                ingresosTotales += escuela.cant_alumnos * INGRESO_POR_ALUMNO
                gastosTotales += escuela.nivel * GASTO_BASE_POR_NIVEL
            }

            val balanceNeto = ingresosTotales - gastosTotales

            // 3. Actualizar el recurso "DINERO"
            val dineroActual = recursoRepository.getRecursoByTipo("DINERO")
            if (dineroActual != null) {
                val nuevoDinero = RecursoEntity(
                    tipo = "DINERO",
                    cantidad = dineroActual.cantidad + balanceNeto,
                    descripcion = dineroActual.descripcion
                )
                recursoRepository.updateRecurso(nuevoDinero)
            }
        }
    }
}
