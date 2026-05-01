package io.moviles.IPN_Tycoon.viewmodels

import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity
import io.moviles.IPN_Tycoon.data.local.entities.RecursoEntity
import io.moviles.IPN_Tycoon.data.repositories.EscuelaRepository
import io.moviles.IPN_Tycoon.data.repositories.RecursoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel central que gestiona el estado global del juego.
 * Refactorizado para usar StateFlow (compatible con módulos no-Android).
 */
class GameStateViewModel(
    private val escuelaRepository: EscuelaRepository,
    private val recursoRepository: RecursoRepository
) {
    // Exponemos los flujos de datos directamente (son compatibles con libGDX)
    val escuelas = escuelaRepository.allEscuelas
    val recursos = recursoRepository.allRecursos

    // Estado para notificaciones (equivalente a LiveData)
    private val _notificacion = MutableStateFlow<String>("")
    val notificacion: StateFlow<String> = _notificacion.asStateFlow()

    fun triggerPrueba() {
        _notificacion.value = "¡Arquitectura StateFlow conectada!"
    }
}
