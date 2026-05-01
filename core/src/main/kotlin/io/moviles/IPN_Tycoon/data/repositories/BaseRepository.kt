package io.moviles.IPN_Tycoon.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository {
    protected val ioContext = Dispatchers.IO

    suspend fun <T> safeDbCall(call: suspend () -> T): T {
        return withContext(ioContext) {
            call()
        }
    }
}
