package io.moviles.IPN_Tycoon.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.moviles.IPN_Tycoon.data.local.daos.EscuelaDao
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity

@Database(entities = [EscuelaEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun escuelaDao(): EscuelaDao
}
