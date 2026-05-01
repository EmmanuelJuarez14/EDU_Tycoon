package io.moviles.IPN_Tycoon.data.repositories

import io.moviles.IPN_Tycoon.data.local.daos.RecursoDao
import io.moviles.IPN_Tycoon.data.local.entities.RecursoEntity
import kotlinx.coroutines.flow.Flow

class RecursoRepository(private val recursoDao: RecursoDao) : BaseRepository() {
    val allRecursos: Flow<List<RecursoEntity>> = recursoDao.getAllRecursos()

    suspend fun getRecursoByTipo(tipo: String): RecursoEntity? = safeDbCall {
        recursoDao.getRecursoByTipo(tipo)
    }

    suspend fun insertRecurso(recurso: RecursoEntity) = safeDbCall {
        recursoDao.insertRecurso(recurso)
    }

    suspend fun updateRecurso(recurso: RecursoEntity) = safeDbCall {
        recursoDao.updateRecurso(recurso)
    }
}
