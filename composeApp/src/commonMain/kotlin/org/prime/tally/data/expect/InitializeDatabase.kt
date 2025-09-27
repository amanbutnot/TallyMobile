package org.prime.tally.data.expect

import org.tally.TallyDatabase


expect fun initializeDatabase(byteArray: ByteArray): TallyDatabase

object DatabaseHolder {
    lateinit var instance: TallyDatabase
        private set

    fun init(byteArray: ByteArray) {
        instance = initializeDatabase(byteArray)
    }
}


