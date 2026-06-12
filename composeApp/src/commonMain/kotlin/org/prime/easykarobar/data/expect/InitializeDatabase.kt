package org.prime.easykarobar.data.expect

import org.prime.easykarobar.TallyDatabase


expect fun initializeDatabase(byteArray: ByteArray): TallyDatabase

object DatabaseHolder {
    private var _instance: TallyDatabase? = null

    val instance: TallyDatabase
        get() {
            return _instance ?: tryAutoInit()
        }

    private fun tryAutoInit(): TallyDatabase {
        val bytes = readFileBytes()
        if (bytes != null) {
            val db = initializeDatabase(bytes)
            _instance = db
            return db
        }
        throw IllegalStateException("DatabaseHolder.instance has not been initialized. Please ensure the database is initialized before access.")
    }

    fun init(byteArray: ByteArray) {
        _instance = initializeDatabase(byteArray)
    }
}
