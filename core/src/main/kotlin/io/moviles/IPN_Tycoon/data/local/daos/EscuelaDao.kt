package io.moviles.IPN_Tycoon.data.local.daos

import androidx.room.*
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity

@Dao
interface EscuelaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(escuela: EscuelaEntity)

    @Query("SELECT * FROM escuelas WHERE slot = :slot LIMIT 1")
    suspend fun getBySlot(slot: Int): EscuelaEntity?

    @Query("DELETE FROM escuelas WHERE slot = :slot")
    suspend fun eliminarSlot(slot: Int)
}
