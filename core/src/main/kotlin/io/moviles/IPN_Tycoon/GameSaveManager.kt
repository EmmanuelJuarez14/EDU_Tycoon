package io.moviles.IPN_Tycoon

/**
 * Interfaz en el módulo core — sin dependencias de Android.
 * La implementación concreta vive en el módulo android.
 */
interface GameSaveManager {

    /** Guarda la partida en el slot indicado. */
    fun guardar(slot: Int, onDone: (success: Boolean) -> Unit)

    /** Carga los datos de los slots 1 y 2 desde Room. */
    fun cargarSlots(onResult: (slot1: GameSaveData?, slot2: GameSaveData?) -> Unit)

    /** Aplica un save al GameState global y al PropiedadRepository. */
    fun cargarPartida(slot: Int, onDone: (success: Boolean) -> Unit)

    /** Borra el save de un slot. */
    fun eliminarSlot(slot: Int, onDone: () -> Unit)
}

/** DTO plano sin dependencias de Room, vive en core. */
data class GameSaveData(
    val slot: Int,
    val nombreJugador: String,
    val nombreEscuela: String,
    val dinero: Long,
    val alumnosTotales: Int,
    val ciclosJugados: Int,
    val fechaGuardado: Long   // epoch ms
)
