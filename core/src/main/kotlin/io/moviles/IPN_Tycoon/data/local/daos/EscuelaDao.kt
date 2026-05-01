package io.moviles.IPN_Tycoon.data.local.daos

import androidx.room.*
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EscuelaDao {
    @Query("SELECT * FROM escuelas")
    fun getAllEscuelas(): Flow<List<EscuelaEntity>>

    @Query("SELECT * FROM escuelas WHERE id = :id")
    suspend fun getEscuelaById(id: Int): EscuelaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscuela(escuela: EscuelaEntity)

    @Update
    suspend fun updateEscuela(escuela: EscuelaEntity)

    @Query("DELETE FROM escuelas")
    suspend fun deleteAll()
}
