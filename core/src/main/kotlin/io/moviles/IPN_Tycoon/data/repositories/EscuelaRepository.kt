package io.moviles.IPN_Tycoon.data.repositories

import io.moviles.IPN_Tycoon.data.local.daos.EscuelaDao
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity
import kotlinx.coroutines.flow.Flow

class EscuelaRepository(private val escuelaDao: EscuelaDao) : BaseRepository() {
    val allEscuelas: Flow<List<EscuelaEntity>> = escuelaDao.getAllEscuelas()

    suspend fun getEscuelaById(id: Int): EscuelaEntity? = safeDbCall {
        escuelaDao.getEscuelaById(id)
    }

    suspend fun insertEscuela(escuela: EscuelaEntity) = safeDbCall {
        escuelaDao.insertEscuela(escuela)
    }

    suspend fun updateEscuela(escuela: EscuelaEntity) = safeDbCall {
        escuelaDao.updateEscuela(escuela)
    }
}
