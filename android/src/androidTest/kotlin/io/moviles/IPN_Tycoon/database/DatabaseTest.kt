package io.moviles.IPN_Tycoon.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.moviles.IPN_Tycoon.android.database.AppDatabase
import io.moviles.IPN_Tycoon.data.local.entities.EscuelaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        // Usamos una base de datos en memoria para que se borre al terminar la prueba
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries() // Solo para pruebas
        .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadEscuela() = runBlocking {
        val escuela = EscuelaEntity(id = 1, nombre = "ESCOM", nivel = 5, cant_alumnos = 100)
        db.escuelaDao().insertEscuela(escuela)

        val allEscuelas = db.escuelaDao().getAllEscuelas().first()
        assertEquals("ESCOM", allEscuelas[0].nombre)
        assertEquals(100, allEscuelas[0].cant_alumnos)
    }
}
