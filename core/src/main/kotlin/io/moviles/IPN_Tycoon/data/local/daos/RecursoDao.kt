package io.moviles.IPN_Tycoon.data.local.daos

import androidx.room.*
import io.moviles.IPN_Tycoon.data.local.entities.RecursoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecursoDao {
    @Query("SELECT * FROM recursos")
    fun getAllRecursos(): Flow<List<RecursoEntity>>

    @Query("SELECT * FROM recursos WHERE tipo = :tipo")
    suspend fun getRecursoByTipo(tipo: String): RecursoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurso(recurso: RecursoEntity)

    @Update
    suspend fun updateRecurso(recurso: RecursoEntity)
}
