package io.moviles.IPN_Tycoon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "escuelas")
data class EscuelaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val nivel: Int = 0,
    val cant_alumnos: Int = 0,
    val reputacion: Float = 50.0f,
    val comprada: Boolean = false
)
