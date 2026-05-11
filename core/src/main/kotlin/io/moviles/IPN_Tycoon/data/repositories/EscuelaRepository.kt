package io.moviles.IPN_Tycoon.data.repositories

import io.moviles.IPN_Tycoon.data.local.daos.EscuelaDao
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity

class EscuelaRepository(private val dao: EscuelaDao) {
    suspend fun guardar(escuela: EscuelaEntity)    = dao.guardar(escuela)
    suspend fun getBySlot(slot: Int): EscuelaEntity? = dao.getBySlot(slot)
    suspend fun eliminarSlot(slot: Int)            = dao.eliminarSlot(slot)
}
