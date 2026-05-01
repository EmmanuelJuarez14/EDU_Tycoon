package io.moviles.IPN_Tycoon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recursos")
data class RecursoEntity(
    @PrimaryKey val tipo: String, // Ej: "DINERO", "PRESTIGIO"
    val cantidad: Long,
    val descripcion: String
)
