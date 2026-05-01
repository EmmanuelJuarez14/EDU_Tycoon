package io.moviles.IPN_Tycoon.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.moviles.IPN_Tycoon.data.local.daos.EscuelaDao
import io.moviles.IPN_Tycoon.data.local.daos.RecursoDao
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity
import io.moviles.IPN_Tycoon.data.local.entities.RecursoEntity

@Database(entities = [EscuelaEntity::class, RecursoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun escuelaDao(): EscuelaDao
    abstract fun recursoDao(): RecursoDao
}
