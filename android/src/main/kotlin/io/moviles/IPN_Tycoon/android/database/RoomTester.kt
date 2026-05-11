package io.moviles.IPN_Tycoon.android.database

import android.content.Context
import android.util.Log
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity
import io.moviles.IPN_Tycoon.data.repositories.EscuelaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RoomTester {
    /**
     * Método para probar que toda la cadena (DAO + Repository) funciona correctamente.
     */
    fun testDatabase(context: Context) {
        val db = DatabaseProvider.getDatabase(context)
        val repository = EscuelaRepository(db.escuelaDao())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("RoomTest", "--- Iniciando prueba de Repository ---")

                // Se actualiza para coincidir con los campos reales de EscuelaEntity
                val testEscuela = EscuelaEntity(
                    slot = 1,
                    nombreJugador = "Jugador de Prueba",
                    nombreEscuela = "ESCOM vía Repository",
                    dinero = 2000000L,
                    edificiosJson = "escom:1",
                    ciclosJugados = 5,
                    alumnosTotales = 1000
                )

                // Se usan los métodos reales: guardar() y getBySlot()
                repository.guardar(testEscuela)
                Log.d("RoomTest", "Escuela guardada correctamente vía Repositorio.")

                val recuperada = repository.getBySlot(1)
                if (recuperada != null && recuperada.nombreEscuela == "ESCOM vía Repository") {
                    Log.d("RoomTest", "¡PRUEBA EXITOSA! Datos recuperados: ${recuperada.nombreEscuela}")
                } else {
                    Log.e("RoomTest", "¡PRUEBA FALLIDA! No se pudieron recuperar los datos.")
                }

                Log.d("RoomTest", "--- Fin de prueba ---")
            } catch (e: Exception) {
                Log.e("RoomTest", "Error durante la prueba: ${e.message}", e)
            }
        }
    }
}
