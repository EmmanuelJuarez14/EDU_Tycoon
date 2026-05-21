package io.moviles.IPN_Tycoon.engine

sealed class EventoEfecto {
    data class Gasto(val cantidad: Long)   : EventoEfecto()
    data class Ingreso(val cantidad: Long) : EventoEfecto()
}

data class GameEvent(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val probabilidad: Float,
    val efecto: EventoEfecto,
    val requiereEdificios: Int = 1
)
