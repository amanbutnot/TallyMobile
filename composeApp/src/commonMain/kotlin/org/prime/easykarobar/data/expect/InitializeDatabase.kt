package org.prime.easykarobar.data.expect

import org.prime.easykarobar.TallyDatabase


expect fun initializeDatabase(byteArray: ByteArray): TallyDatabase

object DatabaseHolder {
    lateinit var instance: TallyDatabase
        private set

    fun init(byteArray: ByteArray) {
        instance = initializeDatabase(byteArray)
    }
}


