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

                val testEscuela = EscuelaEntity(
                    id = 101,
                    nombre = "ESCOM vía Repository",
                    nivel = 1,
                    cant_alumnos = 3500,
                    reputacion = 85.0f,
                    comprada = true
                )

                // Operación a través del repositorio
                repository.insertEscuela(testEscuela)
                Log.d("RoomTest", "Escuela insertada correctamente vía Repositorio.")

                val recuperada = repository.getEscuelaById(101)
                if (recuperada != null && recuperada.nombre == "ESCOM vía Repository") {
                    Log.d("RoomTest", "¡PRUEBA EXITOSA! Datos recuperados: ${recuperada.nombre}")
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
